package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ExposedThingAction
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ExposedThingEvent
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty.*
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.ThingDescription
import ai.ancf.lmos.wot.thing.schema.ThingProperty
import ai.ancf.lmos.wot.thing.schema.ThingProperty.*
import ai.anfc.lmos.wot.binding.ProtocolClient
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Represents an "Exposed Thing" in the Web of Things (WoT) architecture, which extends a base [Thing] instance
 * to add interactive capabilities. This class enables interaction with the [Thing]'s properties, actions, and events
 * by exposing methods to read and write property values, invoke actions, and subscribe to events.
 *
 * @constructor Creates an [ExposedThing] based on an existing [Thing]. Initializes internal maps for properties,
 * actions, and events based on the provided [thing] and allows access to them through additional functionality.
 *
 * @param thing A base [Thing] instance to be exposed with interactive capabilities.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class ExposedThing(private val thing: Thing = Thing()) : ThingDescription by thing {

    val exposedProperties : MutableMap<String, ExposedThingProperty<*>> = mutableMapOf()
    val exposedActions : MutableMap<String, ExposedThingAction<*, *>> = mutableMapOf()
    val exposedEvents : MutableMap<String, ExposedThingEvent<*, *, *>> = mutableMapOf()

    init {
        thing.properties.forEach { (name, property) ->
            val exposedProperty = when (property) {
                is StringProperty -> ExposedStringProperty(thing = thing, property = property)
                is IntProperty -> ExposedIntProperty(thing = thing, property = property)
                is BooleanProperty -> ExposedBooleanProperty(thing = thing, property = property)
                is NumberProperty -> ExposedNumberProperty(thing = thing, property = property)
                is NullProperty -> ExposedNullProperty(thing = thing, property = property)
                is ArrayProperty<*> -> ExposedArrayProperty(thing = thing, property = property)
                is ObjectProperty -> ExposedObjectProperty(thing = thing, property = property)
            }
            exposedProperties[name] = exposedProperty
        }

        thing.actions.forEach { (name, action) ->
            exposedActions[name] = ExposedThingAction(action, thing)
        }

        thing.events.forEach { (name, event) ->
            exposedEvents[name] = ExposedThingEvent(event)
        }
    }

    /**
     * Reads the current values of all properties exposed by the Thing.
     *
     * @return A [Map] of all properties and their values.
     */
    suspend fun readProperties(): Map<String, Any> {
        val values: MutableMap<String, Any> = HashMap()
        exposedProperties.forEach { (name, property) ->
            values[name] = property.read() as Any
        }
        return values
    }

    /**
     * Writes the specified values to the respective properties of the Thing. If successful, returns the new
     * values of the properties.
     *
     * @param values A [Map] of properties which need to be written.
     * @return A [Map] of all properties which have been written.
     */
    suspend fun writeProperties(values: Map<String, Any>): Map<String, Any> {
        val returnValues: MutableMap<String, Any> = HashMap()
        values.forEach { (name, value) ->
            val property = exposedProperties[name]
            if (property != null) {
                // Check if the property's type matches the value's type
                when (property) {
                    is ExposedStringProperty -> if (value is String) {
                        returnValues[name] = property.write(value) as Any
                    }
                    is ExposedIntProperty -> if (value is Int) {
                        returnValues[name] = property.write(value) as Any
                    }
                    is ExposedBooleanProperty -> if (value is Boolean) {
                        returnValues[name] = property.write(value) as Any
                    }
                    is ExposedNumberProperty -> if (value is Number) {
                        returnValues[name] = property.write(value) as Any
                    }
                    is ExposedObjectProperty -> if (value is Map<*, *>) {
                        returnValues[name] = property.write(value as Map<Any, Any>) as Any
                    }
                    is ExposedNullProperty -> {
                        returnValues[name] = property.write(value) as Any
                    }
                    is ExposedArrayProperty<*> -> if (value is List<*>) {
                        //returnValues[name] = property.write(value as List<Any>) as Any
                    }
                }
            }
        }
        // wait until all properties have been written
        return returnValues
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
        fun fromJson(json: String?): ExposedThing? {
            return try {
                JsonMapper.instance.readValue(json, ExposedThing::class.java)
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

private const val s = "https://www.w3.org/2022/wot/td/v1.1"

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
@Serializable
data class Thing (
    @JsonProperty("id") override var id: String = "urn:uuid:" + UUID.randomUUID().toString(),
    @SerialName("@type") @JsonProperty("@type") @JsonInclude(NON_NULL) override var objectType: Type? = Type("Thing"),
    @SerialName("@context") @JsonProperty("@context") @JsonInclude(NON_NULL) override var objectContext: Context? = Context("https://www.w3.org/2022/wot/td/v1.1"),
    @JsonInclude(NON_EMPTY) override var title: String? = null,
    @JsonInclude(NON_EMPTY) override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY) override var description: String? = null,
    @JsonInclude(NON_EMPTY) override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY) override var properties: MutableMap<String, ThingProperty<*>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var actions: MutableMap<String, ThingAction<*, *>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var events: MutableMap<String, ThingEvent<*, *, *>> = mutableMapOf(),
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
) : ThingDescription {

    inline fun <reified T> property(name: String, configure: ThingProperty<T>.() -> Unit) {
        val property: ThingProperty<T> = when (T::class) {
            String::class -> StringProperty() as ThingProperty<T>
            Int::class -> IntProperty() as ThingProperty<T>
            Boolean::class -> BooleanProperty() as ThingProperty<T>
            else -> throw IllegalArgumentException("Unsupported property type")
        }
        property.configure()
        properties[name] = property
    }

    fun Thing.stringProperty(name: String, configure: StringProperty.() -> Unit) {
        this.properties[name] = StringProperty().apply(configure)
    }
    fun Thing.intProperty(name: String, configure: IntProperty.() -> Unit) {
        this.properties[name] = IntProperty().apply(configure)
    }
    fun Thing.booleanProperty(name: String, configure: BooleanProperty.() -> Unit) {
        this.properties[name] = BooleanProperty().apply(configure)
    }
    fun <I : Any, O : Any> Thing.action(name: String, configure: ThingAction<I, O>.() -> Unit) {
        val action = ThingAction<I, O>().apply(configure)
        actions[name] = action
    }

    fun <T, S, C> Thing.event(name: String, configure: ThingEvent<T, S, C>.() -> Unit) {
        val event = ThingEvent<T, S, C>().apply(configure)
        events[name] = event
    }
    /*

    fun getPropertiesByObjectType(objectType: String): Map<String, ThingProperty<Any>> {
        return getPropertiesByExpandedObjectType(getExpandedObjectType(objectType))
    }

    private fun getPropertiesByExpandedObjectType(objectType: String): Map<String, ThingProperty<Any>> {
        return properties.filter { (_, property) ->
            getExpandedObjectType(property.objectType?.defaultType) == objectType
        }.toMap()
    }

    fun getExpandedObjectType(objectType: String?): String {

        val parts = objectType.split(":", limit = 2)
        val prefix = if (parts.size == 2) parts[0] else null
        val suffix = parts.last()

        return objectContext?.getUrl(prefix)?.let { "$it#$suffix" } ?: objectType
    }
    */

    fun toJson() : String?{
        return try {
            JsonMapper.instance.writeValueAsString(this)
        } catch (e: JsonProcessingException) {
            log.warn("Unable to write json", e)
            null
        }
    }

    fun getClientFor(forms: MutableList<Form>?, invokeAction: Operation): Pair<ProtocolClient, Form> {
        TODO("Not yet implemented")
    }

    companion object {
        private val log = LoggerFactory.getLogger(Thing::class.java)

        /**
         * Parses a JSON string into a Thing object.
         *
         * @param json JSON string to parse.
         * @return A Thing instance if parsing is successful, otherwise null.
         */
        fun fromJson(json: String?): Thing? {
            return try {
                JsonMapper.instance.readValue(json, Thing::class.java)
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
        fun fromJson(file: File?): Thing? {
            return try {
                JsonMapper.instance.readValue(file, Thing::class.java)
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
        fun fromMap(map: Map<*, *>): Thing {
            return JsonMapper.instance.convertValue(map, Thing::class.java)
        }
    }
}


/**
 * Data class representing version information of a Thing Description (TD) and its underlying Thing Model (TM).
 *
 * @property instance Provides a version indicator of this TD. This field is mandatory.
 * @property model Provides a version indicator of the underlying TM. This field is optional.
 */
@Serializable
data class VersionInfo(
    val instance: String, // Mandatory: string
    val model: String? = null // Optional: string
)

/**
 * DSL function to create and configure a [Thing] instance.
 *
 * @param id The unique identifier for the Thing. Defaults to a random UUID.
 * @param configure Lambda expression to configure the Thing properties.
 * @return Configured [Thing] instance.
 */
fun thing(id: String = "urn:uuid:" + UUID.randomUUID().toString(), configure: Thing.() -> Unit): Thing {
    return Thing(id = id).apply(configure)
}

