package ai.ancf.lmos.wot.binding.websocket

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import ai.anfc.lmos.wot.binding.Resource
import ai.anfc.lmos.wot.binding.ResourceType
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.jackson.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class WebSocketProtocolClient(
    private val httpClientConfig: HttpClientConfig? = null,
    private val client: HttpClient = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
        }
    }
) : ProtocolClient {
    companion object {
        private val log = LoggerFactory.getLogger(WebSocketProtocolClient::class.java)
    }

    private val sessionCache = ConcurrentHashMap<String, DefaultClientWebSocketSession>()
    private val cacheMutex = Mutex()
    private val resourceChannels = ConcurrentHashMap<String, Channel<Content>>()
    private val requestHandlers = ConcurrentHashMap<String, CompletableDeferred<Content>>()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        log.error("Caught exception: ${throwable.message}", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.IO + exceptionHandler + SupervisorJob())

    override suspend fun start() {
        log.info("Starting WebSocketProtocolClient")
    }

    override suspend fun stop() {
        log.info("Stopping WebSocketProtocolClient")
        cacheMutex.withLock {
            sessionCache.values.forEach { session ->
                try {
                    session.close()
                } catch (e: Exception) {
                    log.warn("Error closing WebSocket session: ${e.message}", e)
                }
            }
            sessionCache.clear()
        }
        scope.cancel() // Cancel all ongoing coroutines
    }

    override suspend fun readResource(resource: Resource): Content {
        return requestAndReply(resource.form, ReadPropertyMessage(resource.thingId, property = resource.name))
    }

    override suspend fun writeResource(resource: Resource, content: Content) {
        requestAndReply(resource.form, WritePropertyMessage(resource.thingId, property = resource.name, data = JsonMapper.instance.readTree(content.body)))
    }

    override suspend fun invokeResource(resource: Resource, content: Content?): Content {
        return requestAndReply(resource.form, InvokeActionMessage(resource.thingId, action = resource.name, input = JsonMapper.instance.readTree(content?.body)))
    }

    override suspend fun unlinkResource(resource: Resource, resourceType: ResourceType) {
        val message = when (resourceType) {
            ResourceType.PROPERTY -> ObservePropertyMessage(thingId = resource.thingId, property =  resource.name)
            ResourceType.EVENT -> SubscribeEventMessage(thingId = resource.thingId, event = resource.name)
        }
        try {
            requestAndReply(resource.form, message)
        } finally {
            resourceChannels[resource.name]?.close()
        }
    }

    override suspend fun subscribeResource(resource: Resource, resourceType: ResourceType): Flow<Content> {
        val message = when (resourceType) {
            ResourceType.PROPERTY -> ObservePropertyMessage(thingId = resource.thingId, property =  resource.name)
            ResourceType.EVENT -> SubscribeEventMessage(thingId = resource.thingId, event = resource.name)
        }
        requestAndReply(resource.form, message)

        val channel = Channel<Content>()
        resourceChannels[resource.name] = channel

        return channel.consumeAsFlow().onCompletion {
            resourceChannels.remove(resource.name)
        }
    }

    private suspend fun getOrCreateSession(href: String): DefaultClientWebSocketSession {
        cacheMutex.withLock {
            sessionCache[href]?.let { return it }
            val newSession = createSession(href)
            launchFrameProcessor(newSession)
            sessionCache[href] = newSession
            return newSession
        }
    }

    private suspend fun createSession(href: String): DefaultClientWebSocketSession {
        return try {
            client.webSocketSession(href)
        } catch (e: Exception) {
            throw ProtocolClientException("Failed to create WebSocket session for $href", e)
        }
    }

    private fun launchFrameProcessor(session: DefaultClientWebSocketSession) {
        scope.launch {
            try {
                for (frame in session.incoming) {
                    if (frame is Frame.Text) {
                        processIncomingMessage(frame.readText())
                    }
                }
            } catch (e: Exception) {
                log.warn("Error processing WebSocket frames", e)
            }
        }
    }

    private suspend fun processIncomingMessage(messageText: String) {
        try {
            when (val woTMessage = JsonMapper.instance.readValue<WoTMessage>(messageText)) {
                is Acknowledgement -> handleReplyMessage(woTMessage)
                is ErrorMessage -> handleErrorMessage(woTMessage)
                is PropertyReadingMessage -> handleReplyMessage(woTMessage)
                is ActionStatusMessage -> handleReplyMessage(woTMessage)
                is EventMessage -> handleEventMessage(woTMessage)
                else -> log.warn("Unhandled message type: ${woTMessage::class.simpleName}")
            }
        } catch (e: Exception) {
            log.warn("Failed to process incoming message", e)
        }
    }

    private suspend fun handleReplyMessage(message: WoTMessage) {
        when (message) {
            is Acknowledgement -> {
                if (message.correlationId != null) {
                    val handler = requestHandlers.remove(message.correlationId)
                    if (handler != null) {
                        handler.complete(Content.EMPTY_CONTENT)
                        return
                    }
                }
                log.warn("No matching request handler for Acknowledgement: ${message.messageId}")
            }
            is PropertyReadingMessage -> {
                if (message.correlationId != null) {
                    val handler = requestHandlers.remove(message.correlationId)
                    if (handler != null) {
                        val content = ContentManager.valueToContent(message.data)
                        handler.complete(content)
                        return
                    }
                }
                // If correlationId is null or no handler found, treat it as an async message
                handlePropertyChangedEvent(message)
            }
            is ActionStatusMessage -> {
                if (message.correlationId != null) {
                    val handler = requestHandlers.remove(message.correlationId)
                    if (handler != null) {
                        val content = ContentManager.valueToContent(message.output)
                        handler.complete(content)
                        return
                    }
                }
                log.warn("No matching request handler for ActionStatusMessage: ${message.messageId}")
            }
            is EventMessage -> {
                // Event messages are always asynchronous
                handleEventMessage(message)
            }
            is ErrorMessage -> {
                if (message.correlationId != null) {
                    val handler = requestHandlers.remove(message.correlationId)
                    if (handler != null) {
                        handler.completeExceptionally(
                            ProtocolClientException("Error received: ${message.title} - ${message.detail}")
                        )
                        return
                    }
                }
                log.warn("Unhandled ErrorMessage without a correlationId: ${message.title}")
            }
            else -> {
                log.warn("Unhandled message type in handleReplyMessage: ${message::class.simpleName}")
            }
        }
    }

    private suspend fun handlePropertyChangedEvent(message: PropertyReadingMessage) {
        val channel = resourceChannels[message.property]
        if (channel != null) {
            val content = ContentManager.valueToContent(message.data)
            channel.send(content)
        } else {
            log.warn("No matching channel for PropertyReadingMessage: ${message.property}")
        }
    }

    private suspend fun handleEventMessage(message: EventMessage) {
        val channel = resourceChannels[message.event]
        if (channel != null) {
            val content = ContentManager.valueToContent(message.data)
            channel.send(content)
        } else {
            log.warn("No matching channel for EventMessage: ${message.event}")
        }
    }

    private fun handleErrorMessage(message: ErrorMessage) {
        val handler = requestHandlers.remove(message.correlationId)
        handler?.completeExceptionally(ProtocolClientException("Error received: ${message.title} - ${message.detail}"))
    }

    private suspend fun requestAndReply(form: Form, message: WoTMessage): Content {
        val session = getOrCreateSession(form.href)
        val deferred = CompletableDeferred<Content>()
        requestHandlers[message.messageId] = deferred
        session.sendSerialized(message)
        return deferred.await()
    }
}