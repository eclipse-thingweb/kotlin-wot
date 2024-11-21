package ai.ancf.lmos.wot.thing


import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.WoTDSL
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.*
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * Represents an "Exposed Thing" in the Web of Things (WoT) architecture, which extends a base [ThingDescription] instance
 * to add interactive capabilities. This class enables interaction with the [ThingDescription]'s properties, actions, and events
 * by exposing methods to read and write property values, invoke actions, and subscribe to events.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@WoTDSL
data class ExposedThing(
    @JsonIgnore
    private val servient: Servient,
    private val thingDescription: ThingDescription = ThingDescription(),
) : WoTExposedThing, WoTThingDescription by thingDescription {


    /**
     * A map of property (read & write) handler callback functions.
     *
     * By using the `private` modifier, this property is excluded from the Thing Description
     * generated by the getThingDescription method.
     */
    private val propertyHandlers: PropertyHandlerMap = ConcurrentHashMap()

    /**
     * A map of action handler callback functions.
     *
     * By using the `private` modifier, this property is excluded from the Thing Description
     * generated by the getThingDescription method.
     */
    private val actionHandlers: ActionHandlerMap = ConcurrentHashMap()

    /**
     * A map of event handler callback functions.
     *
     * By using the `private` modifier, this property is excluded from the Thing Description
     * generated by the getThingDescription method.
     */
    private val eventHandlers: EventHandlerMap = ConcurrentHashMap()

    /**
     * A map of property listener callback functions.
     *
     * By using the `private` modifier, this property is excluded from the Thing Description
     * generated by the getThingDescription method.
     */
    private val propertyListeners: ProtocolListenerRegistry = ProtocolListenerRegistry()

    /**
     * A map of event listener callback functions.
     *
     * By using the `private` modifier, this property is excluded from the Thing Description
     * generated by the getThingDescription method.
     */
    private val eventListenerRegistry: ProtocolListenerRegistry = ProtocolListenerRegistry()

    override fun setPropertyReadHandler(propertyName: String, handler: PropertyReadHandler): ExposedThing {
        log.debug("ExposedThing '${this.title}' setting read handler for '$propertyName'")

        val property = requireNotNull(properties[propertyName]) { "ExposedThing '${this.title}' has no Property '$propertyName'" }
        require(!property.writeOnly) { "ExposedThing '${this.title}' cannot set read handler for property '$propertyName' due to writeOnly flag" }

        propertyHandlers.getOrPut(propertyName) { PropertyHandlers() }.readHandler = handler
        return this
    }

    override fun setPropertyWriteHandler(propertyName: String, handler: PropertyWriteHandler): ExposedThing {
        log.debug("ExposedThing '${this.title}' setting write handler for '$propertyName'")

        val property = requireNotNull(properties[propertyName]) { "ExposedThing '${this.title}' has no Property '$propertyName'" }
        require(!property.readOnly) { "ExposedThing '${this.title}' cannot set write handler for property '$propertyName' due to readOnly flag" }

        propertyHandlers.getOrPut(propertyName) { PropertyHandlers() }.writeHandler = handler
        return this
    }

    override fun setPropertyObserveHandler(propertyName: String, handler: PropertyReadHandler): ExposedThing {
        log.debug("ExposedThing '${this.title}' setting property observe handler for '$propertyName'")

        val property = requireNotNull(properties[propertyName]) { "ExposedThing '${this.title}' has no Property '$propertyName'" }
        require(property.observable) { "ExposedThing '${this.title}' cannot set observe handler for property '$propertyName' since the observable flag is set to false" }

        propertyHandlers.getOrPut(propertyName) { PropertyHandlers() }.observeHandler = handler
        return this
    }

    override fun setPropertyUnobserveHandler(propertyName: String, handler: PropertyReadHandler): ExposedThing {
        log.debug("ExposedThing '${title}' setting property unobserve handler for '$propertyName'")

        val property = properties[propertyName]
        requireNotNull(property) { "ExposedThing '$title' has no Property '$propertyName'" }
        require(property.observable) { "ExposedThing '$title' cannot set unobserve handler for property '$propertyName' since the observable flag is set to false"}

        propertyHandlers.getOrPut(propertyName) { PropertyHandlers() }.unobserveHandler = handler
        return this
    }

    override fun setActionHandler(actionName: String, handler: ActionHandler): ExposedThing {
        log.debug("ExposedThing '${title}' setting action handler for '$actionName'")

        requireNotNull(actions[actionName]) { "ExposedThing '$title' has no Action '$actionName'" }
        actionHandlers[actionName] = handler
        return this
    }

    override fun setEventSubscribeHandler(eventName: String, handler: EventSubscriptionHandler): ExposedThing {
        log.debug("ExposedThing '${title}' setting event subscribe handler for '$eventName'")

        val event = events[eventName]
        require(event != null) { "ExposedThing '$title' has no Event '$eventName'" }

        eventHandlers.getOrPut(eventName) { EventHandlers() }.subscribe = handler
        return this
    }

    override fun setEventUnsubscribeHandler(eventName: String, handler: EventSubscriptionHandler): ExposedThing {
        log.debug("ExposedThing '${title}' setting event unsubscribe handler for '$eventName'")

        val event = events[eventName]
        require(event != null) { "ExposedThing '$title' has no Event '$eventName'" }

        eventHandlers.getOrPut(eventName) { EventHandlers() }.unsubscribe = handler
        return this
    }

    suspend fun handleInvokeAction(
        name: String,
        inputContent: Content,
        options: InteractionOptions = InteractionOptions()
    ): Content {
        val action = requireNotNull(actions[name]) {
            "ExposedThing '$title', no action found for '$name'"
        }
        log.debug("ExposedThing '$title' has Action state of '$name'")

        val handler = requireNotNull(actionHandlers[name]) {
            "ExposedThing '$title' has no handler for Action '$name'"
        }

        log.debug("ExposedThing '$title' calls registered handler for Action '$name'")
        validateInteractionOptions(this, action, options);

        val index = options.formIndex ?: 0
        val contentType = action.forms.getOrNull(index)?.contentType

        val actionResult = handler.handle(InteractionOutput(inputContent, action.input), options)


        return actionResult.let {
            val actionResultValue = it as InteractionInput.Value
            ContentManager.valueToContent(actionResultValue.value, contentType)
        }
    }

    suspend fun handleReadProperty(
        name: String,
        options: InteractionOptions = InteractionOptions()
    ): Content {
        val property = requireNotNull(properties[name]) {
            "ExposedThing '$title', no property found for '$name'"
        }

        val readHandler = requireNotNull(propertyHandlers[name]?.readHandler) {
            "ExposedThing '$title' has no readHandler for Property '$name'"
        }

        log.debug("ExposedThing '$title' calls registered readHandler for Property '$name'")
        validateInteractionOptions(this, property, options);

        val readResult = readHandler.handle(options)
        val index = options.formIndex ?: 0
        val contentType = property.forms.getOrNull(index)?.contentType
        val readResultValue = readResult as InteractionInput.Value
        return ContentManager.valueToContent(readResultValue.value, contentType)
    }

    suspend fun handleWriteProperty(
        name: String,
        inputContent: Content,
        options: InteractionOptions = InteractionOptions()
    ): Content {
        val property = requireNotNull(properties[name]) {
            "ExposedThing '$title', no property found for '$name'"
        }

        val writeHandler = requireNotNull(propertyHandlers[name]?.writeHandler) {
            "ExposedThing '$title' has no writerHandler for Property '$name'"
        }

        log.debug("ExposedThing '$title' calls registered writeHandler for Property '$name'")
        validateInteractionOptions(this, property, options);

        val interactionInput = writeHandler.handle(InteractionOutput(inputContent, property), options)
        val index = options.formIndex ?: 0
        val contentType = property.forms.getOrNull(index)?.contentType
        val inputValue = interactionInput as InteractionInput.Value
        return ContentManager.valueToContent(inputValue.value, contentType)
    }

    /**
     * Handle the request of a read operation for multiple properties from the protocol binding level
     * @experimental
     */
    private suspend fun handleReadProperties(
        propertyNames: List<String>,
        options: InteractionOptions = InteractionOptions()
    ): Map<String, Content> {
        val output = mutableMapOf<String, Content>()
        for (propertyName in propertyNames) {
            val contentResponse = handleReadProperty(propertyName, options)
            output[propertyName] = contentResponse
        }
        return output
    }

    /**
     * Handle the request of a read operation for all properties
     */
    suspend fun handleReadAllProperties(
        options: InteractionOptions = InteractionOptions()
    ): Map<String, Content> {
        val propertyNames = properties.keys.toList()
        return handleReadProperties(propertyNames, options)
    }

    /**
     * Handle the request of a read operation for multiple properties
     */
    suspend fun handleReadMultipleProperties(
        propertyNames: List<String>,
        options: InteractionOptions = InteractionOptions()
    ): Map<String, Content> {
        return handleReadProperties(propertyNames, options)
    }

    suspend fun handleObserveProperty(
        propertyName: String,
        listener: ContentListener,
        options: InteractionOptions = InteractionOptions()
    ) {
        val property = requireNotNull(properties[propertyName]) {
            "ExposedThing '$title', no property found for '$propertyName'"
        }

        // Validate interaction options
        validateInteractionOptions(this, property, options)

        // Get the form index for the operation
        val formIndex = getFormIndexForOperation(
            property, "property", Operation.OBSERVE_PROPERTY, options.formIndex
        )

        if (formIndex != -1) {
            // Register the listener for the property
            propertyListeners.register(property, formIndex, listener)
            println("ExposedThing '$title' subscribes to property '$propertyName'")
        } else {
            throw IllegalArgumentException(
                "ExposedThing '$title', no property listener found for '$propertyName' with form index '${options.formIndex}'"
            )
        }

        // If there's an observe handler, invoke it
        propertyHandlers[propertyName]?.observeHandler?.handle(options)
    }

    suspend fun handleUnobserveProperty(
        propertyName: String,
        listener: ContentListener,
        options: InteractionOptions = InteractionOptions()
    ) {
        val property = requireNotNull(properties[propertyName]) {
            "ExposedThing '$title', no property found for '$propertyName'"
        }

        // Validate interaction options
        validateInteractionOptions(this, property, options)

        // Get the form index for the operation
        val formIndex = getFormIndexForOperation(
            property, "property", Operation.OBSERVE_PROPERTY, options.formIndex
        )

        if (formIndex != -1) {
            // Register the listener for the property
            propertyListeners.unregister(property, formIndex, listener)
            println("ExposedThing '$title' subscribes to property '$propertyName'")
        } else {
            throw IllegalArgumentException(
                "ExposedThing '$title', no property listener found for '$propertyName' with form index '${options.formIndex}'"
            )
        }

        // If there's an observe handler, invoke it
        propertyHandlers[propertyName]?.unobserveHandler?.handle(options)
    }

    override suspend fun emitEvent(eventName: String, data: InteractionInput) {
        val event = requireNotNull(events[eventName]) {
            "ExposedThing '$title', no event found for '$eventName'"
        }
        eventListenerRegistry.notify(event, data, event.data)
    }

    override suspend fun emitPropertyChange(propertyName: String, data: InteractionInput) {
        val property = requireNotNull(properties[propertyName]) {
            "ExposedThing '$title', no property found for '$propertyName'"
        }
        propertyListeners.notify(property, data, property)
    }

    fun handleSubscribeEvent(
        eventName: String,
        listener: ContentListener,
        options: InteractionOptions = InteractionOptions()
    ) {
        val event = requireNotNull(events[eventName]) {
            "ExposedThing '$title', no event found for '$eventName'"
        }
        validateInteractionOptions(this, event, options)

        val formIndex = getFormIndexForOperation(
            event,
            "event",
            Operation.SUBSCRIBE_EVENT,
            options.formIndex
        )

        if (formIndex != -1) {
            eventListenerRegistry.register(event, formIndex, listener)
            log.debug("ExposedThing '${this.title}' subscribes to event '$eventName'")
        } else {
            throw IllegalArgumentException(
                "ExposedThing '${this.title}', no property listener found for '$eventName' with form index '${options.formIndex}'"
            )
        }

        eventHandlers[eventName]?.subscribe?.handle(options)
    }

    fun handleUnsubscribeEvent(
        eventName: String,
        listener: ContentListener,
        options: InteractionOptions = InteractionOptions()
    ) {
        val event = requireNotNull(events[eventName]) {
            "ExposedThing '$title', no event found for '$eventName'"
        }
        validateInteractionOptions(this, event, options)

        val formIndex = getFormIndexForOperation(
            event,
            "event",
            Operation.UNSUBSCRIBE_EVENT,
            options.formIndex
        )

        if (formIndex != -1) {
            eventListenerRegistry.unregister(event, formIndex, listener)
        } else {
            throw IllegalArgumentException(
                "ExposedThing '${this.title}', no event listener found for '$eventName' with form index '${options.formIndex}'"
            )
        }
        val unsubscribe = eventHandlers[eventName]?.unsubscribe?.handle(options)
        log.debug("ExposedThing '${this.title}' unsubscribes from event '$eventName'")
    }

    @JsonIgnore
    override fun getThingDescription(): WoTThingDescription {
        return this
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExposedThing

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

    fun toJson() : String?{
        return try {
            JsonMapper.instance.writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            log.warn("Unable to write json", e)
            null
        }
    }


    companion object {
        private val log = LoggerFactory.getLogger(ExposedThing::class.java)



        /**
         * Parses a JSON string into a Thing object.
         *
         * @param json JSON string to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(json: String): ThingDescription {
            return try {
                JsonMapper.instance.readValue(json, ThingDescription::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                throw e
            }
        }

        /**
         * Parses a JSON file into a Thing object.
         *
         * @param file JSON file to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(file: File?): ExposedThing? {
            return try {
                JsonMapper.instance.readValue(file, ExposedThing::class.java)
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
        fun fromMap(map: Map<*, *>): ExposedThing {
            return JsonMapper.instance.convertValue(map, ExposedThing::class.java)
        }
    }
}

private const val DEFAULT_CONTEXT = "https://www.w3.org/2022/wot/td/v1.1"
private const val DEFAULT_TYPE = "Thing"

/**
 * Represents a Thing entity in the Web of Things (WoT) model.
 *
 * A `Thing` is a digital representation of a physical or abstract entity, containing metadata, capabilities,
 * and affordances such as properties, actions, and events.
 *
 * @property id Unique identifier for the Thing, typically a UUID URI.
 * @property objectType Specifies the type of the Thing using a URI.
 * @property objectContext Defines the JSON-LD context to expand terms within the Thing's description.
 * @property title Human-readable title of the Thing.
 * @property titles Multilingual titles of the Thing, with language codes as keys.
 * @property description Human-readable description of the Thing.
 * @property descriptions Multilingual descriptions of the Thing, with language codes as keys.
 * @property properties A map of properties that describe the state of the Thing.
 * @property actions A map of actions that the Thing can perform.
 * @property events A map of events that the Thing can emit.
 * @property forms List of interaction patterns that specify how to interact with the Thing.
 * @property security Security mechanisms used by the Thing, represented by identifiers defined in securityDefinitions.
 * @property securityDefinitions Definitions of security schemes, specifying how security is applied to interactions.
 * @property base Base URI to resolve relative URIs within the Thing description.
 * @property version Version information for the Thing description.
 * @property created Creation timestamp for the Thing in ISO 8601 format.
 * @property modified Last modification timestamp for the Thing in ISO 8601 format.
 * @property support URL to obtain support for the Thing.
 * @property links List of additional links to resources related to the Thing.
 * @property profile List of profiles that describe the capabilities and interactions of the Thing.
 * @property schemaDefinitions Definitions for data schemas used by the Thing’s properties, actions, or events.
 * @property uriVariables URI variables for dynamic references within forms.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@WoTDSL
data class ThingDescription (
    @JsonProperty("id") override var id: String = getRandomThingId(),
    @SerialName("@type") @JsonProperty("@type") @JsonInclude(NON_NULL) override var objectType: Type? = Type(
        DEFAULT_TYPE),
    @SerialName("@context") @JsonProperty("@context") @JsonInclude(NON_NULL) override var objectContext: Context? = Context(
        DEFAULT_CONTEXT),
    @JsonInclude(NON_EMPTY) override var title: String? = null,
    @JsonInclude(NON_EMPTY) override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY) override var description: String? = null,
    @JsonInclude(NON_EMPTY) override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY) override var properties: MutableMap<String, PropertyAffordance<*>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var actions: MutableMap<String, ActionAffordance<*, *>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var events: MutableMap<String, EventAffordance<*, *, *>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var forms: List<Form> = emptyList(),
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) @JsonInclude(NON_EMPTY) override var security: List<String> = emptyList(),
    @JsonInclude(NON_EMPTY) override var securityDefinitions: MutableMap<String, SecurityScheme> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var base: String? = null,
    @JsonInclude(NON_EMPTY) override var version: VersionInfo? = null,
    @JsonInclude(NON_EMPTY) override var created: String? = null,
    @JsonInclude(NON_EMPTY) override var modified: String? = null,
    @JsonInclude(NON_EMPTY) override var support: String? = null,
    @JsonInclude(NON_EMPTY) override var links: List<Link>? = null,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) @JsonInclude(NON_EMPTY) override var profile: List<String>? = null,
    @JsonInclude(NON_EMPTY) override var schemaDefinitions: MutableMap<String, DataSchema<@Contextual Any>>? = null,
    @JsonInclude(NON_EMPTY) override var uriVariables: MutableMap<String, DataSchema<@Contextual Any>>? = null
) : WoTThingDescription {


    fun stringProperty(name: String, configure: StringProperty.() -> Unit) {
        this.properties[name] = StringProperty().apply(configure)
    }
    fun intProperty(name: String, configure: IntProperty.() -> Unit) {
        this.properties[name] = IntProperty().apply(configure)
    }

    fun booleanProperty(name: String, configure: BooleanProperty.() -> Unit) {
        this.properties[name] = BooleanProperty().apply(configure)
    }
    fun objectProperty(name: String, configure: ObjectProperty.() -> Unit) {
        this.properties[name] = ObjectProperty().apply(configure)
    }
    fun numberProperty(name: String, configure: NumberProperty.() -> Unit) {
        this.properties[name] = NumberProperty().apply(configure)
    }

    fun nullProperty(name: String, configure: NullProperty.() -> Unit) {
        this.properties[name] = NullProperty().apply(configure)
    }

    fun <T> arrayProperty(name: String, configure: ArrayProperty<T>.() -> Unit) {
        this.properties[name] = ArrayProperty<T>().apply(configure)
    }

    fun <I : Any, O : Any> action(name: String, configure: ThingAction<I, O>.() -> Unit) {
        val action = ThingAction<I, O>().apply(configure)
        actions[name] = action
    }

    fun <T, S, C> event(name: String, configure: ThingEvent<T, S, C>.() -> Unit) {
        val event = ThingEvent<T, S, C>().apply(configure)
        events[name] = event
    }

    fun basicSecurityScheme(name: String, configure: BasicSecurityScheme.() -> Unit) {
        val securityScheme = BasicSecurityScheme().apply(configure)
        securityDefinitions[name] = securityScheme
    }

    fun getPropertiesByObjectType(objectType: String): Map<String, PropertyAffordance<*>> {
        return getPropertiesByExpandedObjectType(getExpandedObjectType(objectType))
    }

    private fun getPropertiesByExpandedObjectType(objectType: String): Map<String, PropertyAffordance<*>> {
        return properties.filter { (_, property) ->
            property.objectType?.defaultType?.let { getExpandedObjectType(it) } == objectType
        }.toMap()
    }

    fun getExpandedObjectType(objectType: String): String {

        val parts = objectType.split(":", limit = 2)
        val prefix = if (parts.size == 2) parts[0] else null
        val suffix = parts.last()

        return objectContext?.getUrl(prefix)?.let { "$it#$suffix" } ?: objectType
    }

    fun toJson() : String?{
        return try {
            JsonMapper.instance.writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            log.warn("Unable to write json", e)
            null
        }
    }



    companion object {
        private val log = LoggerFactory.getLogger(ThingDescription::class.java)

        /**
         * Parses a JSON string into a Thing object.
         *
         * @param json JSON string to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(json: String): ThingDescription {
            return try {
                JsonMapper.instance.readValue(json, ThingDescription::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                throw e
            }
        }

        /**
         * Parses a JSON file into a Thing object.
         *
         * @param file JSON file to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(file: File?): ThingDescription? {
            return try {
                JsonMapper.instance.readValue(file, ThingDescription::class.java)
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
        fun fromMap(map: Map<*, *>): ThingDescription {
            return JsonMapper.instance.convertValue(map, ThingDescription::class.java)
        }
    }
}


/**
 * DSL function to create and configure a [ThingDescription] instance.
 *
 * @param id The unique identifier for the Thing. Defaults to a random UUID.
 * @param configure Lambda expression to configure the Thing properties.
 * @return Configured [ThingDescription] instance.
 */
fun thingDescription(configure: ThingDescription.() -> Unit): ThingDescription {
    return ThingDescription().apply(configure)
}

/**
 * DSL function to create and configure a [ExposedThing] instance.
 *
 * @param id The unique identifier for the Thing. Defaults to a random UUID.
 * @param configure Lambda expression to configure the Thing properties.
 * @return Configured [ExposedThing] instance.
 */
fun exposedThing(servient: Servient, id: String = getRandomThingId(), configure: ThingDescription.() -> Unit): ExposedThing {
    // Create the Thing and pass it to ExposedThingImpl
    val thingDescriptionInstance = ThingDescription(id = id).apply(configure)

    // Now pass the created Thing to ExposedThingImpl constructor
    return ExposedThing(
        servient = servient,
        thingDescription = thingDescriptionInstance // Set the configured Thing
    )
}

private fun getRandomThingId() = "urn:uuid:" + UUID.randomUUID().toString()

fun validateInteractionOptions(
    thing: WoTThingDescription,
    ti: InteractionAffordance,
    options: InteractionOptions? = null
): Boolean {
    val interactionUriVariables = ti.uriVariables ?: emptyMap()
    val thingUriVariables = thing.uriVariables ?: emptyMap()

    options?.uriVariables?.let { uriVariables ->
        for ((key, _) in uriVariables) {
            if (key !in interactionUriVariables && key !in thingUriVariables) {
                return false
            }
        }
    }
    return true
}



