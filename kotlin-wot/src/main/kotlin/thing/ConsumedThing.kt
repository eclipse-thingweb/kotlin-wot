package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.ServientException
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.parseInteractionOptions
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.*
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import javax.xml.transform.ErrorListener

/**
 * Represents an object that extends a Thing with methods for client interactions (send request for
 * reading and writing Properties), invoke Actions, subscribe and unsubscribe for Property changes
 * and Events. https://w3c.github.io/wot-scripting-api/#the-consumedthing-interface
 */
data class ConsumedThing(
    @JsonIgnore
    private val servient: Servient,
    private val thingDescription: WoTThingDescription = ThingDescription(),
) : WoTConsumedThing, WoTThingDescription by thingDescription  {

    override suspend fun readProperty(propertyName: String, options: InteractionOptions?): WoTInteractionOutput {
        val property = this.properties[propertyName]
        requireNotNull(property) { "ConsumedThing '${this.title}' does not have property $propertyName" }

        return try {
            // Ensure the property exists
            // Retrieve the client and form for the property
            val (client, form) = getClientFor(property.forms, Operation.READ_PROPERTY)

            // Log the action
            log.debug("ConsumedThing '{}' reading {}", this.title, form.href)

            // Handle URI variables if present
            //val finalForm = handleUriVariables(this, property, form, options)

            // Use the client to read the resource
            val content = client.readResource(form)

            // Process and handle the interaction output

            handleInteractionOutput(content, form, property)
        } catch (e: Exception) {
            throw ConsumedThingException("Error while processing property for ${property.title}. ${e.message}", e)
        }
    }

    private fun handleInteractionOutput(
        content: Content,
        form: Form,
        outputDataSchema: DataSchema<*>?
    ): WoTInteractionOutput {

        // Check if returned media type matches the expected media type from TD
        form.response?.let { response ->
            if (content.type != response.contentType) {
                throw IllegalArgumentException(
                    "Unexpected type '${content.type}' in response. Expected '${response.contentType}'"
                )
            }
        }

        return InteractionOutput(content, outputDataSchema)
    }

    override suspend fun readAllProperties(options: InteractionOptions?): PropertyReadMap {
        TODO("Not yet implemented")
    }

    override suspend fun readMultipleProperties(
        propertyNames: List<String>,
        options: InteractionOptions?
    ): PropertyReadMap {
        TODO("Not yet implemented")
    }

    override suspend fun writeProperty(propertyName: String, value: InteractionInput, options: InteractionOptions?) {
        val property = this.properties[propertyName]
        requireNotNull(property) { "ConsumedThing '${this.title}' does not have property $propertyName" }

        return try {
            // Ensure the property exists
            // Retrieve the client and form for the property
            val (client, form) = getClientFor(property.forms, Operation.WRITE_PROPERTY)

            // Log the action
            log.debug("ConsumedThing '{}' reading {}", this.title, form.href)

            // Handle URI variables if present
            //val finalForm = handleUriVariables(this, property, form, options)

            val interactionValue = value as InteractionInput.Value

            val content = ContentManager.valueToContent(interactionValue.value, form.contentType)

            client.writeResource(form, content)

        } catch (e: Exception) {
            throw ConsumedThingException("Error while processing property for ${property.title}. ${e.message}", e)
        }
    }

    override suspend fun writeMultipleProperties(valueMap: PropertyWriteMap, options: InteractionOptions?) {
        TODO("Not yet implemented")
    }

    override suspend fun invokeAction(
        actionName: String,
        params: InteractionInput,
        options: InteractionOptions?
    ): WoTInteractionOutput {
        val action = this.actions[actionName]
        requireNotNull(action) { "ConsumedThing '${this.title}' does not have action $actionName" }

        return try {
            // Retrieve the client and form for the property
            val (client, form) = getClientFor(action.forms, Operation.INVOKE_ACTION)

            // Log the action
            log.debug("ConsumedThing '{}' invoke {}", this.title, form.href)

            // Handle URI variables if present
            //val finalForm = handleUriVariables(this, property, form, options)

            val interactionValue = params as InteractionInput.Value

            val content = ContentManager.valueToContent(interactionValue.value, form.contentType)

            val response = client.invokeResource(form, content)

            InteractionOutput(response, action.output)

        } catch (e: Exception) {
            throw ConsumedThingException("Error while invoking action for ${action.title}. ${e.message}", e)
        }
    }

    override suspend fun observeProperty(
        name: String,
        listener: InteractionListener,
        onError: ErrorListener?,
        options: InteractionOptions?
    ): Subscription {
        TODO("Not yet implemented")
    }

    override suspend fun subscribeEvent(
        name: String,
        listener: InteractionListener,
        onError: ErrorListener?,
        options: InteractionOptions?
    ): Subscription {
        TODO("Not yet implemented")
    }

    @JsonIgnore
    override fun getThingDescription(): WoTThingDescription {
        return this
    }


    private val clients: MutableMap<String, ProtocolClient> = mutableMapOf()

    fun getClientFor(
        form: Form,
        op: Operation
    ): Pair<ProtocolClient?, Form> {
        return getClientFor(listOf(form), op)
    }

    /**
     * Searches and returns a ProtocolClient in given `forms` that matches the given
     * `op`. Throws an exception when no client can be found.
     *
     * @param forms
     * @param op
     * @return
     * @throws ConsumedThingException
     */

    override fun getClientFor(forms: List<Form>, op: Operation): Pair<ProtocolClient, Form> {
        require(forms.isNotEmpty()) { "No forms available for operation $op on $id" }

        // Get supported schemes in the order of preference
        val supportedSchemes = servient.getClientSchemes()

        // Find schemes in forms sorted by supported schemes preference
        val schemes = forms.asSequence()
            .mapNotNull { it.hrefScheme }
            .distinct()
            .sortedBy { supportedSchemes.indexOf(it).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
            .toSet()

        require(schemes.isNotEmpty()) { "No schemes in forms found" }

        // Try finding a cached client for one of the schemes
        val (scheme, client) = schemes
            .firstNotNullOfOrNull { scheme -> clients[scheme]?.let { scheme to it } }
            ?: initNewClientFor(schemes).also { (newScheme, newClient) ->
                clients[newScheme] = newClient
                log.debug("'$id' got new client for scheme '$newScheme'")
            }

        log.debug("'$id' chose client for scheme '$scheme'")

        val form = getFormForOpAndScheme(forms, op, scheme)
        return client to form
    }


    private fun initNewClientFor(schemes: Set<String>): Pair<String, ProtocolClient> {
        try {
            schemes.forEach { scheme ->
                val client = servient.getClientFor(scheme)
                if (client != null) {
                    // Initialize client security system if security details are provided
                    security.takeIf { it.isNotEmpty() }?.let {
                        log.debug("'{}' setting credentials for '{}'", id, client)
                        val metadata = security.mapNotNull { key -> securityDefinitions[key] }
                        client.setSecurity(metadata, mapOf( "credentials" to servient.getCredentials(id)))
                    }
                    return scheme to client
                }
            }
            throw NoClientFactoryForSchemesConsumedThingException(id, schemes)
        } catch (e: ProtocolClientException) {
            throw ConsumedThingException("Unable to create client: ${e.message}")
        }
    }

    private fun getFormForOpAndScheme(
        forms: List<Form>,
        op: Operation,
        scheme: String?
    ): Form {
        // Find a form that matches the operation and scheme
        return forms.firstOrNull { it.op?.contains(op) == true && it.hrefScheme == scheme }
            ?: forms.firstOrNull { it.op.isNullOrEmpty() && it.hrefScheme == scheme }
            ?: throw NoFormForInteractionConsumedThingException(id, op)
    }

    suspend fun readProperties(vararg names: String): Map<*, *> {
        return readProperties(listOf(*names))
    }

    /**
     * Returns the values of the properties contained in `names`.
     *
     * @param names
     * @return
     */
    suspend fun readProperties(names: List<String>): Map<*, *> {
        val values = readProperties()

        return values.filter { (key, _) -> key in names }
            .mapKeys { it.key }
    }
    /*

    /**
     * Returns the values of all properties.
     *
     * @return
     */
    suspend fun readProperties(): Map<*, *> {
        val clientAndForm: Pair<ProtocolClient, Form> = getClientFor(forms, Operation.READ_ALL_PROPERTIES)
        val client = clientAndForm.first
        val form: Form = clientAndForm.second
        log.debug("'{}' reading '{}'", id, form.href)
        val result = client.readResource(form)
        try {
            return ContentManager.contentToValue(result, ObjectSchema())
        } catch (e: ContentCodecException) {
            throw CompletionException(ConsumedThingException("Received invalid writeResource from Thing: " + e.message))
        }
    }
     */

    /**
     * Creates new form (if needed) for URI Variables http://192.168.178.24:8080/counter/actions/increment{?step}
     * with '{'step' : 3}' -&gt; http://192.168.178.24:8080/counter/actions/increment?step=3.<br></br>
     * see RFC6570 (https://tools.ietf.org/html/rfc6570) for URI Template syntax
     */
    fun handleUriVariables(form: Form, parameters: Map<String, Any>): Form {
        val href: String = form.href
        val uriTemplate: UriTemplate = UriTemplate.fromTemplate(href)
        val updatedHref: String = uriTemplate.expand(parameters)
        if (href != updatedHref) {
            // "clone" form to avoid modifying original form
            val updatedForm = Form(updatedHref)
            log.debug("'{}' update URI to '{}'", href, updatedHref)
            return updatedForm
        }
        return form
    }

    fun handleUriVariables(
        thing: WoTConsumedThing,
        ti: InteractionAffordance,
        form: Form,
        options: InteractionOptions?
    ): Form {
        val uriTemplate = UriTemplate.fromTemplate(form.href)
        val uriVariables = parseInteractionOptions(thingDescription, ti, options).uriVariables ?: emptyMap()
        val updatedHref = uriTemplate.expand(uriVariables)

        return if (updatedHref != form.href) {
            // Create a shallow copy and update href
            form.copy(href = updatedHref).also {
                log.debug("ConsumedThing '${thingDescription.title}' updated form URI to ${it.href}")
            }
        } else {
            form
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConsumedThing

        if (id != other.id) return false
        if (objectType != other.objectType) return false
        if (objectContext != other.objectContext) return false
        if (title != other.title) return false
        if (titles != other.titles) return false
        if (description != other.description) return false
        if (descriptions != other.descriptions) return false
        if (properties != other.properties) return false
        if (actions != other.actions) return false
        if (events != other.events) return false
        if (forms != other.forms) return false
        if (security != other.security) return false
        if (securityDefinitions != other.securityDefinitions) return false
        if (base != other.base) return false
        if (version != other.version) return false
        if (created != other.created) return false
        if (modified != other.modified) return false
        if (support != other.support) return false
        if (links != other.links) return false
        if (profile != other.profile) return false
        if (schemaDefinitions != other.schemaDefinitions) return false
        if (uriVariables != other.uriVariables) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (objectType?.hashCode() ?: 0)
        result = 31 * result + (objectContext?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (titles?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (descriptions?.hashCode() ?: 0)
        result = 31 * result + properties.hashCode()
        result = 31 * result + actions.hashCode()
        result = 31 * result + events.hashCode()
        result = 31 * result + forms.hashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + securityDefinitions.hashCode()
        result = 31 * result + (base?.hashCode() ?: 0)
        result = 31 * result + (version?.hashCode() ?: 0)
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (modified?.hashCode() ?: 0)
        result = 31 * result + (support?.hashCode() ?: 0)
        result = 31 * result + (links?.hashCode() ?: 0)
        result = 31 * result + (profile?.hashCode() ?: 0)
        result = 31 * result + (schemaDefinitions?.hashCode() ?: 0)
        result = 31 * result + (uriVariables?.hashCode() ?: 0)
        return result
    }


    companion object {
        private val log = LoggerFactory.getLogger(ConsumedThing::class.java)

        /**
         * Parses a JSON string into a Thing object.
         *
         * @param json JSON string to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(json: String?): ConsumedThing? {
            return try {
                JsonMapper.instance.readValue(json, ConsumedThing::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                null
            }
        }

        /**
         * Parses a JSON file into a Thing object.
         *
         * @param file JSON file to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(file: File?): ConsumedThing? {
            return try {
                JsonMapper.instance.readValue(file, ConsumedThing::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                null
            }
        }

        /**
         * Converts a Map into a Thing object.
         *
         * @param map Map representing the Thing structure.
         * @return A Thing instance converted from the map.
         */
        fun fromMap(map: Map<*, *>): ConsumedThing {
            return JsonMapper.instance.convertValue(map, ConsumedThing::class.java)
        }
    }
}

class NoFormForInteractionConsumedThingException : ConsumedThingException {
    constructor(title: String, op: Operation) : super("'$title' has no form for interaction '$op'")
    constructor(message: String) : super(message)
}


class NoClientFactoryForSchemesConsumedThingException(title: String, schemes: Set<String?>) :
    ConsumedThingException("'$title': Missing ClientFactory for schemes '$schemes'")

open class ConsumedThingException : ServientException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable?) : super(cause)

    constructor(message: String, cause: Throwable?) : super(cause)
}
