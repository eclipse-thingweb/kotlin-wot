package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.action.ExposedThingAction
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.InteractionAffordance
import ai.anfc.lmos.wot.binding.Content
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
    private val actualAddresses: List<String> = listOf()

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

        for (address in actualAddresses) {
            for (contentType in ContentManager.getOfferedMediaTypes()) {
                // make reporting of all properties optional?
                val href = (address + "/" + thing.id).toString() + "/all/properties"
                val form = Form(href = href, contentType = contentType, op =  listOf( Operation.READ_ALL_PROPERTIES,
                    Operation.READ_MULTIPLE_PROPERTIES))

                thing.forms += form
                log.debug("Assign '{}' for reading all properties", href)

                exposeProperties(thing, address, contentType)
                exposeActions(thing, address, contentType)
                exposeEvents(thing, address, contentType)
            }
        }
    }

    fun exposeProperties(thing: ExposedThing, address: String, contentType: String) {
        val properties = thing.exposedProperties

        properties.forEach { (name, property) ->

            val href = getHrefWithVariablePattern(address, thing, "properties", name, property)

            // Determine the operations based on readOnly/writeOnly status
            val operations = when {
                property.readOnly -> listOf(Operation.READ_PROPERTY)
                property.writeOnly -> listOf(Operation.WRITE_PROPERTY)
                else -> listOf(Operation.READ_PROPERTY, Operation.WRITE_PROPERTY)
            }

            // Create the main form and add it to the property
            val form = Form(href = href, contentType = contentType, op = operations)
            property.forms?.plusAssign(form)
            log.debug("Assign '{}' to Property '{}'", href, name)

            // If the property is observable, add an additional form with an observable href
            if (property.observable) {
                val observableHref = "$href/observable"
                val observableForm = Form(
                    href = observableHref,
                    contentType = contentType,
                    op = listOf(Operation.OBSERVE_PROPERTY),
                    subprotocol = "longpoll"
                )
                property.forms?.plusAssign(observableForm)
                log.debug("Assign '{}' to observe Property '{}'", observableHref, name)
            }
        }
    }

    fun exposeActions(thing: ExposedThing, address: String, contentType: String) {
        val actions: Map<String, ExposedThingAction<*, *>> = thing.exposedActions
        actions.forEach { (name, action) ->
            val href: String = getHrefWithVariablePattern(address, thing, "actions", name, action)
            // Initialize the form using named parameters
            val form = Form(
                href = href,
                contentType = contentType,
                op = listOf(Operation.INVOKE_ACTION)
            )

            // Add the form to the action
            action.forms?.plusAssign(form)
            log.debug("Assign '{}' to Action '{}'", href, name)
        }
    }

    fun exposeEvents(thing: ExposedThing, address: String, contentType: String) {
        val events = thing.exposedEvents
        events.forEach { (name, event) ->
            val href = getHrefWithVariablePattern(address, thing, "events", name, event)

            // Create the form using named parameters directly
            val form = Form(
                href = href,
                contentType = contentType,
                subprotocol = "longpoll",
                op = listOf(Operation.SUBSCRIBE_EVENT)
            )

            // Add the form to the event
            event.forms?.plusAssign(form)
            log.debug("Assign '{}' to Event '{}'", href, name)
        }
    }

    private fun getHrefWithVariablePattern(
        address: String,
        thing: ExposedThing,
        type: String,
        interactionName: String,
        interaction: InteractionAffordance
    ): String {
        var variables = ""
        val uriVariables = interaction.uriVariables?.keys
        if (uriVariables != null) {
            variables = "{?" + java.lang.String.join(",", uriVariables) + "}"
        }
        return "$address/${thing.id}/$type/$interactionName$variables"
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
                    val property = thing.exposedProperties[propertyName]
                    if (property != null) {
                       // call.respond(property.read())
                    } else {
                        call.response.status(HttpStatusCode.NotFound)
                    }
                }
                put {
                    val id = call.parameters["id"] ?: return@put call.response.status(HttpStatusCode.BadRequest)
                    val propertyName = call.parameters["name"]
                    val thing = servient.things[id] ?: return@put call.response.status(HttpStatusCode.NotFound)
                    val property = thing.exposedProperties[propertyName]
                    if (property != null) {
                        if (!property.readOnly) {
                            val contentType = getOrDefaultRequestContentType(call.request)
                            val content = Content(contentType.toString(), call.receiveText())
                            val input = ContentManager.contentToValue(content, property)

                            //val newValue = property.write(input)
                            //call.respond(newValue)
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
                val action = thing.exposedActions[actionName]
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

private fun getOrDefaultRequestContentType(request: RoutingRequest): ContentType {
    val contentType = request.contentType()
    // Check if the content type is of type `Any` and return the default
    return if (contentType == ContentType.Any) {
        ContentType.Application.Json
    } else {
        contentType
    }
}