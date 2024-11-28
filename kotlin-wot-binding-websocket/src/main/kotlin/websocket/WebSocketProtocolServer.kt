package websocket

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.anfc.lmos.wot.binding.ProtocolServer
import ai.anfc.lmos.wot.binding.ProtocolServerException
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory
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
    install(WebSockets)
    routing {
        webSocket("/ws/things/{id}") {
            val thingId = call.parameters["id"]
            val propertyName = call.parameters["name"]

            if (thingId == null || propertyName == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid parameters"))
                return@webSocket
            }

            val thing = servient.things[thingId]
            val property = thing?.properties?.get(propertyName)

            if (thing == null || property == null || !property.observable) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Thing or property not found, or not observable"))
                return@webSocket
            }

            val session = this
            val contentListener = ContentListener { content ->
                try {
                    session.send(Frame.Text(String(content.body)))
                } catch (e: Exception) {
                    log.warn("Error sending WebSocket message: ${e.message}")
                }
            }

            thing.handleObserveProperty(propertyName, contentListener)
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Close) {
                        thing.handleUnobserveProperty(propertyName, contentListener)
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnected"))
                    }
                }
            } finally {
                thing.handleUnobserveProperty(propertyName, contentListener)
            }
        }

        webSocket("/things/{id}/events/{name}/subscribe") {
            val thingId = call.parameters["id"]
            val eventName = call.parameters["name"]

            if (thingId == null || eventName == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid parameters"))
                return@webSocket
            }

            val thing = servient.things[thingId]
            val event = thing?.events?.get(eventName)

            if (thing == null || event == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Thing or event not found"))
                return@webSocket
            }

            val session = this
            val contentListener = ContentListener { content ->
                try {
                    session.send(Frame.Text(String(content.body)))
                } catch (e: Exception) {
                    log.warn("Error sending WebSocket message: ${e.message}")
                }
            }

            thing.handleSubscribeEvent(eventName, contentListener)
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Close) {
                        thing.handleUnsubscribeEvent(eventName, contentListener)
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnected"))
                    }
                }
            } finally {
                thing.handleUnsubscribeEvent(eventName, contentListener)
            }
        }
    }
}