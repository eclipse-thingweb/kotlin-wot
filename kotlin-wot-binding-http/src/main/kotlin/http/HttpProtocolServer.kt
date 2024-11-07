package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentCodecException
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThingImpl
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty.*
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
import java.util.concurrent.ExecutionException
import kotlin.collections.set

/**
 * Allows exposing Things via HTTP.
 */
open class HttpProtocolServer(
    private val wait: Boolean = false,
    private val bindHost: String = "0.0.0.0",
    private val bindPort: Int = 8080,
    private val createServer: (host: String, port: Int, servient: Servient) -> EmbeddedServer<*, *> = ::defaultServer
) : ProtocolServer {
    val things: MutableMap<String, ExposedThingImpl> = mutableMapOf()
    var started = false
    private var server: EmbeddedServer<*, *>? = null
    private var actualAddresses: List<String> = listOf("http://$bindHost:$bindPort")

    companion object {
        private val log = LoggerFactory.getLogger(HttpProtocolServer::class.java)
        private const val HTTP_METHOD_NAME = "htv:methodName"
    }

    override suspend fun start(servient: Servient) {
        log.info("Starting on '{}' port '{}'", bindHost, bindPort)
        started = true
        server = createServer(bindHost, bindPort, servient).start(wait)
    }

    // Stop the server
    override suspend fun stop() {
        if (!started) throw ProtocolServerException("Server has not started yet")
        log.info("Stopping on '{}' port '{}'", bindHost, bindPort)
        server?.stop(1000, 2000)
        started = false
    }

    // Expose a thing
    override fun expose(thing: ExposedThingImpl) {
        if (!started) throw ProtocolServerException("Server has not started yet")

        log.info("Exposing thing '{}'", thing.id)
        things[thing.id] = thing

        for (address in actualAddresses) {
            for (contentType in ContentManager.offeredMediaTypes) {
                // make reporting of all properties optional?
                val href = "$address/${thing.id}/all/properties"
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

    internal fun exposeProperties(thing: ExposedThingImpl, address: String, contentType: String) {
        thing.properties.forEach { (name, property) ->

            val href = getHrefWithVariablePattern(address, thing, "properties", name, property)

            // Determine the operations based on readOnly/writeOnly status
            val operations = when {
                property.readOnly -> listOf(Operation.READ_PROPERTY)
                property.writeOnly -> listOf(Operation.WRITE_PROPERTY)
                else -> listOf(Operation.READ_PROPERTY, Operation.WRITE_PROPERTY)
            }

            // Determine the HTTP method based on property attributes
            val httpMethod = when {
                property.readOnly -> "GET"
                property.writeOnly -> "PUT"
                else -> null // No specific HTTP method for both read/write properties
            }

            // Create the main form and add it to the property
            val form = Form(
                href = href,
                contentType = contentType,
                op = operations,
                optionalProperties = hashMapOf<String, String>().apply {
                    httpMethod?.let { put(HTTP_METHOD_NAME, it) }
                }
            )
            property.forms += form
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
                property.forms += observableForm
                log.debug("Assign '{}' to observe Property '{}'", observableHref, name)
            }
        }
    }

    internal fun exposeActions(thing: ExposedThingImpl, address: String, contentType: String) {
        thing.actions.forEach { (name, action) ->
            val href: String = getHrefWithVariablePattern(address, thing, "actions", name, action)
            // Initialize the form using named parameters
            val form = Form(
                href = href,
                contentType = contentType,
                op = listOf(Operation.INVOKE_ACTION)
            )

            // Add the form to the action
            action.forms += form
            log.debug("Assign '{}' to Action '{}'", href, name)
        }
    }

    internal fun exposeEvents(thing: ExposedThingImpl, address: String, contentType: String) {
        thing.events.forEach { (name, event) ->
            val href = getHrefWithVariablePattern(address, thing, "events", name, event)

            // Create the form using named parameters directly
            val form = Form(
                href = href,
                contentType = contentType,
                subprotocol = "longpoll",
                op = listOf(Operation.SUBSCRIBE_EVENT)
            )

            // Add the form to the event
            event.forms += form
            log.debug("Assign '{}' to Event '{}'", href, name)
        }
    }

    private fun getHrefWithVariablePattern(
        address: String,
        thing: ExposedThingImpl,
        type: String,
        interactionName: String,
        interaction: InteractionAffordance
    ): String {
        var variables = ""
        val uriVariables = interaction.uriVariables?.keys
        if (!uriVariables.isNullOrEmpty()) {
            variables = "{?" + java.lang.String.join(",", uriVariables) + "}"
        }
        return "$address/${thing.id}/$type/$interactionName$variables"
    }

    // Destroy a thing
    override suspend fun destroy(thing: ExposedThingImpl) {
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
                call.respond(servient.things.values.toList(), typeInfo<List<ExposedThingImpl>>())
            }
        }
        route("/{id}") {
            get {
                val id = call.parameters["id"]
                val thing: ExposedThingImpl? = servient.things[id]
                if (thing != null) {
                    call.respond(thing, typeInfo<ExposedThingImpl>())
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
                        if (!property.writeOnly) {
                             try {
                                val value = property.read()
                                //contentType = getOrDefaultRequestContentType(call.request)
                                //val content = ContentManager.valueToContent(value, contentType.toString())
                                 when (property) {
                                     is ExposedStringProperty -> call.respond(property.read(), typeInfo<String>())
                                     is ExposedIntProperty -> call.respond(property.read(), typeInfo<Int>())
                                     is ExposedBooleanProperty -> call.respond(property.read(), typeInfo<Boolean>())
                                     is ExposedNumberProperty -> call.respond(property.read(), typeInfo<Double>())
                                     is ExposedObjectProperty -> call.respond(property.read(), typeInfo<Map<*,*>>())
                                     is ExposedNullProperty -> call.respond(property.read(), typeInfo<Any>())
                                     is ExposedArrayProperty -> call.respond(property.read(), typeInfo<List<*>>())
                                 }
                            }
                             catch (e: ContentCodecException) {
                                 call.response.status(HttpStatusCode.InternalServerError)
                            } catch (e: ExecutionException) {
                                call.response.status(HttpStatusCode.InternalServerError)
                            }
                        } else {
                            call.response.status(HttpStatusCode.BadRequest)
                        }
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
                            //val contentType = getOrDefaultRequestContentType(call.request)
                            //val content = Content(contentType.toString(), call.receiveChannel().toByteArray())

                            when (property) {
                                is ExposedStringProperty -> call.respond(property.write(call.receive<String>()), typeInfo<String>())
                                is ExposedIntProperty -> call.respond(property.write(call.receive<Int>()), typeInfo<Int>())
                                is ExposedBooleanProperty -> call.respond(property.write(call.receive<Boolean>()), typeInfo<Boolean>())
                                is ExposedNumberProperty -> call.respond(property.write(call.receive<Double>()), typeInfo<Double>())
                                is ExposedObjectProperty -> call.respond(property.write(call.receive<Map<*,*>>()), typeInfo<Map<*,*>>())
                                is ExposedNullProperty -> call.respond(property.write(call.receive<Any>()), typeInfo<Any>())
                                is ExposedArrayProperty -> call.respond(property.write(call.receive<List<*>>()), typeInfo<List<*>>())
                            }
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
                    val requestContentType = getOrDefaultRequestContentType(call.request).toString()
                    val content = Content(requestContentType, call.receive())

                    if(action.input != null){
                        val input = ContentManager.contentToValue(content, action.input!!)
                        val newValue = action.invokeAction(input, null)
                        call.respond(newValue, typeInfo<Any>())
                    }else{
                        val newValue = action.invokeAction(null, null)
                        call.respond(newValue, typeInfo<Any>())
                    }


                    /*
                    val options = mapOf<String, Map<String, Any>>(
                        "uriVariables" to parseUrlParameters(request.queryMap().toMap(), action.uriVariables)
                    )
                    */
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

/*
private fun parseUrlParameters(
    urlParams: Map<String, Array<String>>,
    uriVariables: MutableMap<String, DataSchema<Any>>?
): Map<String, Any> {
    return urlParams.mapNotNull { (name, urlValue) ->
        val uriVariable = uriVariables?.get(name)
        val type = uriVariable["type"]

        val value: Any = when (type) {
            "integer", "number" -> urlValue.firstOrNull()?.toIntOrNull()
            "string" -> urlValue.firstOrNull()
            else -> {
                null
            }
        }
        name to value
    }.toMap()
}
*/