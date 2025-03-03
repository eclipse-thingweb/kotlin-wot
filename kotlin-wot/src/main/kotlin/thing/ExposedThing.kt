/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing


import org.eclipse.thingweb.JsonMapper
import org.eclipse.thingweb.Servient
import org.eclipse.thingweb.WoTDSL
import org.eclipse.thingweb.content.Content
import org.eclipse.thingweb.content.Content.Companion.EMPTY_CONTENT
import org.eclipse.thingweb.content.ContentManager
import org.eclipse.thingweb.thing.form.Operation
import org.eclipse.thingweb.thing.schema.*
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonProcessingException
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
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
    private val thingDescription: WoTThingDescription = ThingDescription(),
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
    private val propertyListeners: SessionAwareProtocolListenerRegistry = SessionAwareProtocolListenerRegistry()

    /**
     * A map of event listener callback functions.
     *
     * By using the `private` modifier, this property is excluded from the Thing Description
     * generated by the getThingDescription method.
     */
    private val eventListenerRegistry: SessionAwareProtocolListenerRegistry = SessionAwareProtocolListenerRegistry()

    fun unregisterAllListeners(sessionId: String){
        propertyListeners.unregisterAll(sessionId)
        eventListenerRegistry.unregisterAll(sessionId)
    }

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

    @WithSpan
    suspend fun handleInvokeAction(
        @SpanAttribute("wot.action.name") name: String,
        inputContent: Content = EMPTY_CONTENT,
        options: InteractionOptions = InteractionOptions()
    ): Content {
        Span.current().setAttribute("wot.thing.id", id)
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

    @WithSpan
    suspend fun handleReadProperty(
        @SpanAttribute("wot.property.name") name: String,
        options: InteractionOptions = InteractionOptions()
    ): Content {
        Span.current().setAttribute("wot.thing.id", id)
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

    @WithSpan
    suspend fun handleWriteProperty(
        @SpanAttribute("wot.property.name") name: String,
        inputContent: Content,
        options: InteractionOptions = InteractionOptions()
    ): Content {
        Span.current().setAttribute("wot.thing.id", id)
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
    @WithSpan
    private suspend fun handleReadProperties(
        propertyNames: List<String>,
        options: InteractionOptions = InteractionOptions()
    ): Map<String, Content> {
        Span.current().setAttribute("wot.thing.id", id)
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
    @WithSpan
    suspend fun handleReadAllProperties(
        options: InteractionOptions = InteractionOptions()
    ): Map<String, Content> {
        Span.current().setAttribute("wot.thing.id", id)
        val propertyNames = properties.keys.toList()
        return handleReadProperties(propertyNames, options)
    }

    /**
     * Handle the request of a read operation for multiple properties
     */
    @WithSpan
    suspend fun handleReadMultipleProperties(
        propertyNames: List<String>,
        options: InteractionOptions = InteractionOptions()
    ): Map<String, Content> {
        Span.current().setAttribute("wot.thing.id", id)
        return handleReadProperties(propertyNames, options)
    }

    @WithSpan
    suspend fun handleObserveProperty(
        sessionId: String = "default",
        @SpanAttribute("wot.property.name") propertyName: String,
        listener: ContentListener,
        options: InteractionOptions = InteractionOptions()
    ) {
        Span.current().setAttribute("wot.thing.id", id)
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
            propertyListeners.register(sessionId, property, formIndex, listener)
            log.debug("Property listener registered for '$propertyName'")
        } else {
            throw IllegalArgumentException(
                "ExposedThing '$title', no property listener found for '$propertyName' with form index '${options.formIndex}'"
            )
        }

        // If there's an observe handler, invoke it
        propertyHandlers[propertyName]?.observeHandler?.handle(options)
    }

    @WithSpan
    suspend fun handleUnobserveProperty(
        sessionId: String = "default",
        @SpanAttribute("wot.property.name") propertyName: String,
        options: InteractionOptions = InteractionOptions()
    ) {
        Span.current().setAttribute("wot.thing.id", id)
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
            propertyListeners.unregister(sessionId, property, formIndex)
            log.debug("Property listener unregistered for '$propertyName'")
        } else {
            throw IllegalArgumentException(
                "ExposedThing '$title', no property listener found for '$propertyName' with form index '${options.formIndex}'"
            )
        }

        // If there's an observe handler, invoke it
        propertyHandlers[propertyName]?.unobserveHandler?.handle(options)
    }

    @WithSpan
    override suspend fun emitEvent(@SpanAttribute("wot.event.name") eventName: String, data: InteractionInput) {
        Span.current().setAttribute("wot.thing.id", id)
        val event = requireNotNull(events[eventName]) {
            "ExposedThing '$title', no event found for '$eventName'"
        }
        eventListenerRegistry.notify(event, data, event.data)
    }

    @WithSpan
    override suspend fun emitPropertyChange(@SpanAttribute("wot.property.name") propertyName: String, data: InteractionInput) {
        Span.current().setAttribute("wot.thing.id", id)
        val property = requireNotNull(properties[propertyName]) {
            "ExposedThing '$title', no property found for '$propertyName'"
        }
        propertyListeners.notify(property, data, property)
    }

    @WithSpan
    suspend fun handleSubscribeEvent(
        @SpanAttribute("sessionId") sessionId: String = "default",
        @SpanAttribute("wot.event.name") eventName: String,
        listener: ContentListener,
        options: InteractionOptions = InteractionOptions()
    ) {
        Span.current().setAttribute("wot.thing.id", id)
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
            eventListenerRegistry.register(sessionId, event, formIndex, listener)
            log.debug("ExposedThing '${this.title}' subscribes to event '$eventName'")
        } else {
            throw IllegalArgumentException(
                "ExposedThing '${this.title}', no event listener found for '$eventName' with form index '${options.formIndex}'"
            )
        }

        eventHandlers[eventName]?.subscribe?.handle(options)
    }

    @WithSpan
    suspend fun handleUnsubscribeEvent(
        @SpanAttribute("sessionId") sessionId: String = "default",
        @SpanAttribute("wot.event.name") eventName: String,
        options: InteractionOptions = InteractionOptions()
    ) {
        Span.current().setAttribute("wot.thing.id", id)
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
            eventListenerRegistry.unregister(sessionId, event, formIndex)
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

    fun toJson() : String{
        return try {
            JsonMapper.instance.writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            log.warn("Unable to write json", e)
            throw e
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



