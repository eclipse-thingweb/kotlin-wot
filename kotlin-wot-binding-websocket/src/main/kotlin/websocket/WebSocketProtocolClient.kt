package ai.ancf.lmos.wot.binding.websocket


import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import ai.anfc.lmos.wot.binding.Resource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.jackson.*
import io.ktor.websocket.*
import kotlinx.coroutines.CompletableDeferred
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

    // Cache for WebSocket sessions, keyed by href
    private val sessionCache = ConcurrentHashMap<String, DefaultClientWebSocketSession>()
    private val cacheMutex = Mutex()

    override suspend fun start() {
        log.info("Starting WebSocketProtocolClient")
        // No global connection to start. Connections are established per href.
    }

    override suspend fun stop() {
        log.info("Stopping WebSocketProtocolClient")
        // Close all cached sessions
        sessionCache.values.forEach { session ->
            try {
                session.close()
            } catch (e: Exception) {
                log.warn("Error closing WebSocket session: ${e.message}", e)
            }
        }
        sessionCache.clear()
    }

    override suspend fun readResource(resource: Resource): Content {
        return sendMessage(resource.form, ReadPropertyMessage(resource.thingId, property = resource.name))
    }

    override suspend fun writeResource(resource: Resource, content: Content) {
        sendMessage(resource.form,  WritePropertyMessage(resource.thingId, property = resource.name,
            data = JsonMapper.instance.readTree(content.body)
        ))
    }

    override suspend fun invokeResource(resource: Resource, content: Content?): Content {
        return sendMessage(resource.form,  InvokeActionMessage(resource.thingId, action = resource.name,
            input = JsonMapper.instance.readTree(content?.body)
        ))
    }

    private suspend fun sendMessage(form: Form, message: WoTMessage): Content {
        val session = getOrCreateSession(form.href)

        val response = CompletableDeferred<Content>()

        try {
            session.sendSerialized(message)

            when (val woTMessage = session.receiveDeserialized<WoTMessage>()) {
                is ErrorMessage -> throw ProtocolClientException("Error received: ${woTMessage.title} - ${woTMessage.detail}")
                is PropertyReadingMessage -> {
                    val responseContent = ContentManager.valueToContent(woTMessage.data)
                    response.complete(responseContent)
                }
                is ActionStatusMessage -> {
                    val responseContent = ContentManager.valueToContent(woTMessage.output)
                    response.complete(responseContent)
                }
                else -> throw ProtocolClientException("Unexpected message type received: ${woTMessage::class.simpleName}")
            }
        } catch (e: Exception) {
            response.completeExceptionally(ProtocolClientException("Error during WebSocket request: ${e.message}", e))
        }

        return response.await()
    }

    private suspend fun getOrCreateSession(href: String): DefaultClientWebSocketSession {
        cacheMutex.withLock {
            // Perform both the check and the update within the same lock
            sessionCache[href]?.let { return it }
            // If no session exists, create a new one
            val newSession = createSession(href)
            sessionCache[href] = newSession
            return newSession
        }
    }

    private suspend fun createSession(href: String): DefaultClientWebSocketSession {
        try {
            return client.webSocketSession (href)
        } catch (e: Exception) {
            throw ProtocolClientException("Failed to create WebSocket session for $href", e)
        }
    }
}