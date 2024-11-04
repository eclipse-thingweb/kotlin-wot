package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.anfc.lmos.wot.binding.ProtocolServer
import ai.anfc.lmos.wot.binding.ProtocolServerException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import org.slf4j.LoggerFactory
import kotlin.collections.set

/**
 * Allows exposing Things via HTTP.
 */
class HttpProtocolServer(
    private val wait: Boolean = false,
    private val bindHost: String = "0.0.0.0",
    private val bindPort: Int = 8080,
    private val createServer: (host: String, port: Int, servient: Servient) -> EmbeddedServer<*, *> = ::defaultServer
) : ProtocolServer {
    val things: MutableMap<String, ExposedThing> = mutableMapOf()
    var started = false
    private var server: EmbeddedServer<*, *>? = null

    companion object {
        private val log = LoggerFactory.getLogger(HttpProtocolServer::class.java)
    }

    override suspend fun start(servient: Servient) {
        log.info("Starting on '{}' port '{}'", bindHost, bindPort)
        server = createServer(bindHost, bindPort, servient).start(wait)
        started = true
    }

    // Stop the server
    override suspend fun stop() {
        if (!started) throw ProtocolServerException("Server has not started yet")
        log.info("Stopping on '{}' port '{}'", bindHost, bindPort)
        server?.stop(1000, 2000)
        started = false
    }

    // Expose a thing
    override suspend fun expose(thing: ExposedThing) {
        if (!started) throw ProtocolServerException("Server has not started yet")

        log.info("Exposing thing '{}'", thing.id)
        things[thing.id] = thing

        // Add logic to expose the thing's properties, actions, and events via routes
    }

    // Destroy a thing
    override suspend fun destroy(thing: ExposedThing) {
        log.info("Removing thing '{}'", thing.id)
        things.remove(thing.id)
    }
}

// Default server function
fun defaultServer(host: String, port: Int, servient: Servient): EmbeddedServer<*, *> {
    return embeddedServer(Netty, port = port, host = host) {
        setupRouting(servient)
    }
}

fun Application.setupRouting(servient: Servient) {
    setupJackson()

    routing {
        route("/") {
            get {
                call.respond(servient.things.values.toList(), typeInfo<List<ExposedThing>>())
            }
        }
        route("/{id}") {
            get {
                val id = call.parameters["id"]
                val thing: ExposedThing? = servient.things[id]
                if (thing != null) {
                    call.respond(thing, typeInfo<ExposedThing>())
                } else {
                    call.response.status(HttpStatusCode.NotFound)
                }
            }
            route("/properties/{name}") {
                /*
                get("/observable") {
                    call.respond("Observing property", typeInfo<String>())
                }
                */
                get {
                    val id = call.parameters["id"] ?: return@get call.response.status(HttpStatusCode.BadRequest)
                    val propertyName = call.parameters["name"]
                    val thing = servient.things[id] ?: return@get call.response.status(HttpStatusCode.NotFound)
                    val property = thing.properties[propertyName]
                    if (property != null) {
                        call.respond("TODO Return property value", typeInfo<String>())
                    } else {
                        call.response.status(HttpStatusCode.NotFound)
                    }
                }
                put {
                    val id = call.parameters["id"] ?: return@put call.response.status(HttpStatusCode.BadRequest)
                    val propertyName = call.parameters["name"]
                    val thing = servient.things[id] ?: return@put call.response.status(HttpStatusCode.NotFound)
                    val property = thing.properties[propertyName]
                    if (property != null) {
                        if (!property.readOnly) {
                            val newValue = call.receive<Any>()
                            // TODO handle write logic

                            call.response.status(HttpStatusCode.OK)
                        } else {
                            call.response.status(HttpStatusCode.BadRequest)
                        }
                    } else {
                        call.response.status(HttpStatusCode.NotFound)
                    }
                }
            }
            post("/actions/{name}") {
                val id = call.parameters["id"] ?: return@post call.response.status(HttpStatusCode.BadRequest)
                val actionName = call.parameters["name"]
                val thing = servient.things[id] ?: return@post call.response.status(HttpStatusCode.NotFound)
                val action = thing.actions[actionName]
                if (action != null) {
                    val input = call.receive<Any>()
                    call.respond("TODO Return action response", typeInfo<String>())
                } else {
                    call.response.status(HttpStatusCode.NotFound)
                }
            }
            get("/events/{name}") {
                call.response.status(HttpStatusCode.OK)
            }
        }
    }
}

private fun Application.setupJackson() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
}