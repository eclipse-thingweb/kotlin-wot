package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.anfc.lmos.wot.binding.ProtocolClient
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import javax.xml.transform.ErrorListener



fun interface PropertyReadHandler {
    suspend fun handle(options: InteractionOptions?): InteractionInput?
}

fun interface PropertyWriteHandler {
    suspend fun handle(input: WoTInteractionOutput, options: InteractionOptions?): InteractionInput?
}

fun interface ActionHandler {
    suspend fun handle(input: WoTInteractionOutput, options: InteractionOptions?): InteractionInput?
}

// Type Aliases for Mapping and Listener Types
typealias PropertyContentMap = Map<String, Content>
typealias ContentListener = (Content) -> Unit
typealias PropertyHandlerMap = MutableMap<String, PropertyHandlers>
typealias ActionHandlerMap = MutableMap<String, ActionHandler>
typealias EventHandlerMap = MutableMap<String, EventHandlers>
typealias ListenerMap = Map<String, ListenerItem>
typealias PropertyReadMap = Map<String, WoTInteractionOutput>
typealias PropertyWriteMap = Map<String, InteractionInput>

data class InteractionOptions(
    val formIndex: Int? = null,
    val uriVariables: Map<String, Any>? = null,
    val data: Any? = null
)

interface Subscription {
    val active: Boolean
    suspend fun stop(options: InteractionOptions? = null)
}

// Data Structure for Handling Properties, Actions, and Events
data class PropertyHandlers(
    var readHandler: PropertyReadHandler? = null,
    var writeHandler: PropertyWriteHandler? = null,
    var observeHandler: PropertyReadHandler? = null,
    var unobserveHandler: PropertyReadHandler? = null
)

data class EventHandlers (
    var subscribe: EventSubscriptionHandler? = null,
    var unsubscribe: EventSubscriptionHandler? = null
)

data class ListenerItem(
    val formIndexListeners: Map<Int, List<ContentListener>>
)

interface EventSubscriptionHandler {
    suspend fun subscribe()
    suspend fun unsubscribe()
}

// Sealed class to represent either a stream or data schema value
sealed class InteractionInput {
    // Represents a stream of bytes (similar to ReadableStream)
    data class Stream(val stream: InputStream) : InteractionInput()

    // Represents the DataSchemaValue (could be a Boolean, String, etc.)
    data class Value(val value: DataSchemaValue) : InteractionInput()
}

typealias InteractionListener = (data: WoTInteractionOutput) -> Unit

// Interface for InteractionOutput
interface WoTInteractionOutput {
    val data: Flow<ByteArray>? // assuming a stream of data, could be ReadableStream equivalent
    val dataUsed: Boolean
    //val form: Form?
    val schema: DataSchema<*>?
    suspend fun arrayBuffer(): ByteArray
    suspend fun value(): DataSchemaValue?
}



sealed class DataSchemaValue {
    data object NullValue : DataSchemaValue()
    data class BooleanValue(val value: Boolean) : DataSchemaValue()
    data class IntegerValue(val value: Int) : DataSchemaValue()
    data class NumberValue(val value: Number) : DataSchemaValue()
    data class StringValue(val value: String) : DataSchemaValue()
    data class ObjectValue(val value: Map<*, *>) : DataSchemaValue()
    data class ArrayValue(val value: List<*>) : DataSchemaValue()
}



// Interface for ExposedThing, handling various property, action, and event interactions
interface WoTExposedThing {

    fun setPropertyReadHandler(propertyName: String, handler: PropertyReadHandler): WoTExposedThing
    fun setPropertyWriteHandler(propertyName: String, handler: PropertyWriteHandler): WoTExposedThing
    fun setPropertyObserveHandler(propertyName: String, handler: PropertyReadHandler): WoTExposedThing
    fun setPropertyUnobserveHandler(propertyName: String, handler: PropertyReadHandler): WoTExposedThing

    suspend fun emitPropertyChange(propertyName: String, data: InteractionInput? = null)

    fun setActionHandler(actionName: String, handler: ActionHandler): WoTExposedThing

    fun setEventSubscribeHandler(eventName: String, handler: EventSubscriptionHandler): WoTExposedThing
    fun setEventUnsubscribeHandler(eventName: String, handler: EventSubscriptionHandler): WoTExposedThing

    suspend fun emitEvent(eventName: String, data: InteractionInput? = null)

    //suspend fun expose(): Unit
    //suspend fun destroy(): Unit

    fun getThingDescription(): WoTThingDescription
}


const val THING_CONTEXT_TD_URI_V11: String = "https://www.w3.org/2022/wot/td/v1.1"
/**
 * This constant is associated with the `ThingDescription` JSON schema
 * definition "thing-context-td-uri-v1".
 */
const val THING_CONTEXT_TD_URI_V1: String = "https://www.w3.org/2019/wot/td/v1"
/**
 * This constant is associated with the `ThingDescription` JSON schema
 * definition "thing-context-td-uri-temp".
 */
const val THING_CONTEXT_TD_URI_TEMP: String = "http://www.w3.org/ns/td"

interface WoTConsumedThing {

    /**
     * Reads a property by its name.
     * @param propertyName The name of the property to read.
     * @param options Optional interaction options.
     * @return The property value as [WoTInteractionOutput].
     */
    suspend fun readProperty(propertyName: String, options: InteractionOptions? = InteractionOptions()): WoTInteractionOutput

    /**
     * Reads all properties.
     * @param options Optional interaction options.
     * @return A map of property names to their values.
     */
    suspend fun readAllProperties(options: InteractionOptions? = InteractionOptions()): PropertyReadMap

    /**
     * Reads multiple properties by their names.
     * @param propertyNames The list of property names to read.
     * @param options Optional interaction options.
     * @return A map of property names to their values.
     */
    suspend fun readMultipleProperties(propertyNames: List<String>, options: InteractionOptions? = InteractionOptions()): PropertyReadMap

    /**
     * Writes a value to a property by its name.
     * @param propertyName The name of the property to write.
     * @param value The value to write to the property.
     * @param options Optional interaction options.
     */
    suspend fun writeProperty(propertyName: String, value: InteractionInput, options: InteractionOptions? = InteractionOptions())

    /**
     * Writes multiple properties with a map of values.
     * @param valueMap The map of property names and values to write.
     * @param options Optional interaction options.
     */
    suspend fun writeMultipleProperties(valueMap: PropertyWriteMap, options: InteractionOptions? = InteractionOptions())

    /**
     * Invokes an action by its name with optional parameters.
     * @param actionName The name of the action to invoke.
     * @param params Optional parameters for the action.
     * @param options Optional interaction options.
     * @return The result of the action as [WoTInteractionOutput].
     */
    suspend fun invokeAction(actionName: String, params: InteractionInput, options: InteractionOptions? = InteractionOptions()): WoTInteractionOutput

    /**
     * Observes a property by its name, with a callback for each update.
     * @param name The name of the property to observe.
     * @param listener The listener for property changes.
     * @param onError The error listener, optional.
     * @param options Optional interaction options.
     * @return A subscription object for the property observation.
     */
    suspend fun observeProperty(name: String, listener: InteractionListener, onError: ErrorListener? = null, options: InteractionOptions? = InteractionOptions()): Subscription

    /**
     * Subscribes to an event by its name, with a callback for each event occurrence.
     * @param name The name of the event to subscribe to.
     * @param listener The listener for event occurrences.
     * @param onError The error listener, optional.
     * @param options Optional interaction options.
     * @return A subscription object for the event subscription.
     */
    suspend fun subscribeEvent(name: String, listener: InteractionListener, onError: ErrorListener? = null, options: InteractionOptions? = InteractionOptions()): Subscription

    /**
     * Gets the Thing Description associated with this consumed thing.
     * @return The Thing Description.
     */
    fun getThingDescription(): WoTThingDescription

    fun getClientFor(forms: List<Form>, op: Operation): Pair<ProtocolClient, Form>
}