package ai.ancf.lmos.wot.binding.websocket


import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentCodecException
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import ai.anfc.lmos.wot.binding.ProtocolServer
import ai.anfc.lmos.wot.binding.ProtocolServerException
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.reflect.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class WebSocketProtocolServer(
    private val wait: Boolean = false,
    private val bindHost: String = "0.0.0.0",
    private val bindPort: Int = 8080,
    private val createServer: (host: String, port: Int, servient: Servient) -> EmbeddedServer<*, *> = ::defaultWebSocketServer
) : ProtocolServer {
    val things: MutableMap<String, ExposedThing> = mutableMapOf()
    private val webSocketSessions: MutableMap<String, MutableList<WebSocketServerSession>> = ConcurrentHashMap()
    var started = false
    private var server: EmbeddedServer<*, *>? = null
    private var actualAddresses: List<String> = listOf("http://$bindHost:$bindPort")

    companion object {
        private val log = LoggerFactory.getLogger(WebSocketProtocolServer::class.java)
    }

    override suspend fun start(servient: Servient) {
        log.info("Starting WebSocket server on '{}' port '{}'", bindHost, bindPort)
        started = true
        server = createServer(bindHost, bindPort, servient).start(wait)
    }

    override suspend fun stop() {
        if (!started) throw ProtocolServerException("Server has not started yet")
        log.info("Stopping WebSocket server on '{}' port '{}'", bindHost, bindPort)
        server?.stop(1000, 2000)
        started = false
    }

    override suspend fun expose(thing: ExposedThing) {
        if (!started) throw ProtocolServerException("Server has not started yet")

        log.info("Exposing thing '{}'", thing.id)
        things[thing.id] = thing
        for (address in actualAddresses) {
            exposePropertiesWithWebSockets(thing, address)
            exposeActions(thing, address)
            exposeEventsWithWebSockets(thing, address)
        }
    }

    override suspend fun destroy(thing: ExposedThing) {
        log.info("Removing thing '{}'", thing.id)
        things.remove(thing.id)
    }

    private fun exposePropertiesWithWebSockets(thing: ExposedThing, address: String) {
        thing.properties.forEach { (name, property) ->
            if (property.observable) {
                log.debug("Setting up WebSocket observation for property '{}' of thing '{}'", name, thing.id)
            }
        }
    }

    private fun exposeActions(thing: ExposedThing, address: String) {
        thing.actions.forEach { (name, action) ->
            log.debug("Exposing action '{}' of thing '{}'", name, thing.id)
        }
    }

    private fun exposeEventsWithWebSockets(thing: ExposedThing, address: String) {
        thing.events.forEach { (name, event) ->
            log.debug("Setting up WebSocket subscription for event '{}' of thing '{}'", name, thing.id)
        }
    }
}

// Default server setup
fun defaultWebSocketServer(host: String, port: Int, servient: Servient): EmbeddedServer<*, *> {
    return embeddedServer(Netty, port = port, host = host) {
        setupRoutingWithWebSockets(servient)
    }
}

fun Application.setupRoutingWithWebSockets(servient: Servient) {
    install(WebSockets) {
        contentConverter = JacksonWebsocketContentConverter()
    }
    routing {
        route("/") {
            get {
                call.respond(servient.things.values.toList(), typeInfo<List<WoTExposedThing>>())
            }
        }

        webSocket("/ws") {
            try {
                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            try {

                                log.info(frame.readText())

                                // Deserialize the message to WoTMessage
                                val message: WoTMessage = receiveDeserialized<WoTMessage>()
                                // Retrieve the thingId from the message
                                val thingId = message.thingId
                                val thing = servient.things[thingId]
                                if (thing == null) {
                                    val errorJson = createThingNotFound(thingId)
                                    send(Frame.Text(errorJson))
                                    return@webSocket
                                }

                                // Handle the message based on its type
                                when (message) {
                                    is ReadPropertyMessage -> {
                                        val propertyName = message.property
                                        val property = thing.properties[propertyName]
                                        if (property != null) {
                                            if(property.writeOnly){
                                                try {
                                                    val content = thing.handleReadProperty(propertyName)
                                                    val response = PropertyReadingMessage(
                                                        thingId = thingId,
                                                        property = propertyName,
                                                        data = JsonMapper.instance.readTree(content.body)
                                                    )
                                                    sendSerialized(response)
                                                } catch (e: ContentCodecException) {
                                                    val errorJson = createInternalServerError(thingId, e)
                                                    send(Frame.Text(errorJson))
                                                }
                                            }else{
                                                val errorJson = createPropertyIsWriteOnly(thingId, propertyName)
                                                send(Frame.Text(errorJson))
                                            }

                                        } else {
                                            val errorJson = createPropertyNotFound(thingId, propertyName)
                                            send(Frame.Text(errorJson))
                                        }
                                    }

                                    is WritePropertyMessage -> {
                                        val propertyName = message.property
                                        val content = message.data
                                        val property = thing.properties[propertyName]
                                        if (property != null) {
                                            if(!property.readOnly){
                                                try {
                                                    // TODO
                                                    //thing.handleWriteProperty(propertyName, content)
                                                    log.info("Wrote to property '$propertyName': $content")
                                                    send(Frame.Text("Property written successfully"))
                                                } catch (e: ContentCodecException) {
                                                    val errorJson = createInternalServerError(thingId, e)
                                                    send(Frame.Text(errorJson))
                                                }
                                            }else{
                                                val errorJson = createPropertyIsReadOnly(thingId, propertyName)
                                                send(Frame.Text(errorJson))
                                            }
                                        } else {
                                            val errorJson = createPropertyNotFound(thingId, propertyName)
                                            send(Frame.Text(errorJson))
                                        }
                                    }

                                    is ObservePropertyMessage -> {
                                        val propertyName = message.property
                                        val property = thing.properties[propertyName]
                                        if (property != null) {
                                            val contentListener = ContentListener { content ->
                                                try {
                                                    send(Frame.Text(String(content.body)))
                                                } catch (e: Exception) {
                                                    log.warn("Error sending WebSocket message: ${e.message}")
                                                }
                                            }
                                            thing.handleObserveProperty(propertyName, contentListener)
                                            log.info("Started observing property '$propertyName'")
                                        } else {
                                            val errorJson = createPropertyNotFound(thingId, propertyName)
                                            send(Frame.Text(errorJson))
                                        }
                                    }

                                    is UnobservePropertyMessage -> {
                                        val propertyName = message.property
                                        val property = thing.properties[propertyName]
                                        if (property != null) {
                                            val contentListener = ContentListener { content: Content ->
                                                log.info("Stopped observing property '$propertyName'")
                                                send(Frame.Text("Stopped observing property"))
                                            }
                                            thing.handleUnobserveProperty(propertyName, contentListener)

                                        } else {
                                            val errorJson = createPropertyNotFound(thingId, propertyName)
                                            send(Frame.Text(errorJson))
                                        }
                                    }

                                    // Handle other message types...
                                    else -> {
                                        val errorJson = createUnsupportedMessageType(thingId, message)
                                        send(Frame.Text(errorJson))
                                    }
                                }
                            } catch (e: Exception) {
                                val errorMessage = "Unsupported or invalid message type"
                                log.warn(errorMessage, e)
                                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, errorMessage))
                            }
                        }

                        else -> {
                            val errorMessage = "Unsupported frame type: ${frame.frameType}"
                            log.warn(errorMessage)
                            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, errorMessage))
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                log.debug("onClose {}", closeReason.await())
            } catch (e: Exception) {
                log.error("Error in WebSocket handling", e)
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Internal error"))
            }
        }

    }
}

private fun createUnsupportedMessageType(thingId: String, message: WoTMessage): String {
    val errorMessage = ErrorMessage(
        thingId = thingId,
        type = "https://w3c.github.io/web-thing-protocol/errors#unsupported-message-type",
        title = "Unsupported Message Type",
        status = "400",
        detail = "Unsupported message type: ${message.messageType}",
        instance = "https://mythingserver.com/errors/${UUID.randomUUID()}"
    )
    val errorJson = JsonMapper.instance.writeValueAsString(errorMessage)
    return errorJson
}

private fun createPropertyIsReadOnly(thingId: String, propertyName: String): String {
    val errorMessage = ErrorMessage(
        thingId = thingId,
        type = "https://w3c.github.io/web-thing-protocol/errors#operation-not-allowed",
        title = "Property Read-Only",
        status = "405",
        detail = "Property '$propertyName' is read-only.",
        instance = "https://mythingserver.com/errors/${UUID.randomUUID()}"
    )
    val errorJson = JsonMapper.instance.writeValueAsString(errorMessage)
    return errorJson
}

private fun createPropertyIsWriteOnly(thingId: String, propertyName: String): String {
    val errorMessage = ErrorMessage(
        thingId = thingId,
        type = "https://w3c.github.io/web-thing-protocol/errors#operation-not-allowed",
        title = "Property is write-only",
        status = "405",
        detail = "Property '$propertyName' is write-only.",
        instance = "https://mythingserver.com/errors/${UUID.randomUUID()}"
    )
    val errorJson = JsonMapper.instance.writeValueAsString(errorMessage)
    return errorJson
}

private fun createThingNotFound(thingId: String): String {
    val errorMessage = ErrorMessage(
        thingId = thingId,
        type = "https://w3c.github.io/web-thing-protocol/errors#not-found",
        title = "Thing Not Found",
        status = "404",
        detail = "Thing with ID '$thingId' not found.",
        instance = "https://mythingserver.com/errors/${UUID.randomUUID()}"
    )
    val errorJson = JsonMapper.instance.writeValueAsString(errorMessage)
    return errorJson
}

private fun createInternalServerError(thingId: String, e: ContentCodecException): String {
    val errorMessage = ErrorMessage(
        thingId = thingId,
        type = "https://w3c.github.io/web-thing-protocol/errors#internal-server-error",
        title = "Internal Server Error",
        status = "500",
        detail = "Error reading property: ${e.message}",
        instance = "https://mythingserver.com/errors/${UUID.randomUUID()}"
    )
    val errorJson = JsonMapper.instance.writeValueAsString(errorMessage)
    return errorJson
}

private fun createPropertyNotFound(thingId: String, propertyName: String): String {
    val errorMessage = ErrorMessage(
        thingId = thingId,
        type = "https://w3c.github.io/web-thing-protocol/errors#not-found",
        title = "Property Not Found",
        status = "404",
        detail = "Property '$propertyName' not found.",
        instance = "https://mythingserver.com/errors/${UUID.randomUUID()}"
    )
    val errorJson = JsonMapper.instance.writeValueAsString(errorMessage)
    return errorJson
}