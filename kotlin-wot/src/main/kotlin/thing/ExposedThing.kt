package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ExposedThingAction
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ExposedThingEvent
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import ai.ancf.lmos.wot.thing.property.ThingProperty
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
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
data class ExposedThing(private val thing: Thing) : ThingDescription by thing {

    private val _properties : MutableMap<String, ExposedThingProperty<Any>> = mutableMapOf()
    private val _actions : MutableMap<String, ExposedThingAction<Any, Any>> = mutableMapOf()
    private val _events : MutableMap<String, ExposedThingEvent<Any>> = mutableMapOf()

    init {
        thing.properties.forEach { (name, property) ->
            _properties[name] = ExposedThingProperty(property, thing)
        }

        thing.actions.forEach { (name, action) ->
            _actions[name] = ExposedThingAction(action, thing)
        }

        thing.events.forEach { (name, event) ->
            _events[name] = ExposedThingEvent(event)
        }
    }

    /**
     * Reads the current values of all properties exposed by the Thing.
     *
     * @return A [Map] of all properties and their values.
     */
    suspend fun readProperties(): Map<String, Any> {
        val values: MutableMap<String, Any> = HashMap()
        _properties.forEach { (name, property) ->
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
            val property: ExposedThingProperty<Any>? = _properties[name]
            returnValues[name] = property?.write(value) as Any
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
    override val id: String = "urn:uuid:" + UUID.randomUUID().toString(),
    @get:JsonProperty("@type") @JsonInclude(NON_NULL) override var objectType: Type? = null,
    @get:JsonProperty("@context") @JsonInclude(NON_NULL) override var objectContext: Context? = null,
    @JsonInclude(NON_EMPTY) override var title: String? = null,
    @JsonInclude(NON_EMPTY) override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY) override var description: String? = null,
    @JsonInclude(NON_EMPTY) override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY) override var properties: MutableMap<String, ThingProperty<Any>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var actions: MutableMap<String, ThingAction<Any, Any>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var events: MutableMap<String, ThingEvent<Any>> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var forms: List<Form>? = null,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) @JsonInclude(NON_EMPTY) override var security: List<String> = emptyList(),
    @JsonInclude(NON_EMPTY) override var securityDefinitions: MutableMap<String, SecurityScheme> = mutableMapOf(),
    @JsonInclude(NON_EMPTY) override var base: String? = null,
    @JsonInclude(NON_EMPTY) override var version: VersionInfo? = null,
    @JsonInclude(NON_EMPTY) override var created: String? = null,
    @JsonInclude(NON_EMPTY) override var modified: String? = null,
    @JsonInclude(NON_EMPTY) override var support: String? = null,
    @JsonInclude(NON_EMPTY) override var links: List<Link>? = null,
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) @JsonInclude(NON_EMPTY) override var profile: List<String>? = null,
    @JsonInclude(NON_EMPTY) override var schemaDefinitions: MutableMap<String, DataSchema<Any>>? = null,
    @get:JsonInclude(NON_EMPTY) override var uriVariables: MutableMap<String, DataSchema<Any>>? = null
) : ThingDescription {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Thing -> false
            else -> id == other.id
        }
    }

    fun Thing.property(name: String, configure: ThingProperty<Any>.() -> Unit) {
        this.properties[name] = ThingProperty<Any>().apply(configure)
    }

    fun Thing.action(name: String, configure: ThingAction<Any, Any>.() -> Unit) {
        this.actions[name] = ThingAction<Any, Any>().apply(configure)
    }

    fun Thing.event(name: String, configure: ThingEvent<Any>.() -> Unit) {
        this.events[name] = ThingEvent<Any>().apply(configure)
    }

    fun getPropertiesByObjectType(objectType: String?): Map<String, ThingProperty<Any>> {
        return getPropertiesByExpandedObjectType(getExpandedObjectType(objectType))
    }

    fun getPropertiesByExpandedObjectType(objectType: String?): Map<String, ThingProperty<Any>> {
        return properties.filter { (_, property) ->
            getExpandedObjectType(property.objectType) == objectType
        }.toMap()
    }

    fun getExpandedObjectType(objectType: String?): String? {
        if (objectType == null || objectContext == null) {
            return null
        }
        val parts = objectType.split(":", limit = 2)
        val prefix = if (parts.size == 2) parts[0] else null
        val suffix = parts.last()

        return objectContext?.getUrl(prefix)?.let { "$it$suffix" } ?: objectType
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
 * Interface representing a Thing Description (TD) in a Web of Things context.
 */
interface ThingDescription {

    /**
     * JSON-LD keyword to define short-hand names called terms that are used throughout a TD document.
     *
     * @return a URI or an array of URIs representing the context.
     */
    var objectContext: Context? // Optional: anyURI or Array

    /**
     * JSON-LD keyword to label the object with semantic tags (or types).
     *
     * @return a string or an array of strings representing the types.
     */
    var objectType: Type? // Optional: string or Array of string

    /**
     * Identifier of the Thing in form of a URI RFC3986.
     *
     * @return an optional URI identifier.
     */
    val id: String // Optional: anyURI

    /**
     * Provides a human-readable title based on a default language.
     *
     * @return the title of the Thing, which is mandatory.
     */
    var title: String? // Mandatory: string

    /**
     * Provides multi-language human-readable titles.
     *
     * @return a map of multi-language titles.
     */
    var titles: MutableMap<String, String>? // Optional: Map of MultiLanguage

    /**
     * Provides additional (human-readable) information based on a default language.
     *
     * @return an optional description.
     */
    var description: String? // Optional: string

    /**
     * Can be used to support (human-readable) information in different languages.
     *
     * @return a map of descriptions in different languages.
     */
    var descriptions: MutableMap<String, String>? // Optional: Map of MultiLanguage

    /**
     * Provides version information.
     *
     * @return optional version information.
     */
    var version: VersionInfo? // Optional: VersionInfo

    /**
     * Provides information when the TD instance was created.
     *
     * @return the creation date and time.
     */
    var created: String? // Optional: dateTime

    /**
     * Provides information when the TD instance was last modified.
     *
     * @return the last modified date and time.
     */
    var modified: String? // Optional: dateTime

    /**
     * Provides information about the TD maintainer as URI scheme (e.g., mailto, tel, https).
     *
     * @return an optional support URI.
     */
    var support: String? // Optional: anyURI

    /**
     * Define the base URI that is used for all relative URI references throughout a TD document.
     *
     * @return an optional base URI.
     */
    var base: String? // Optional: anyURI

    /**
     * All Property-based Interaction Affordances of the Thing.
     *
     * @return a map of property affordances.
     */
    var properties: MutableMap<String, ThingProperty<Any>> // Optional: Map of PropertyAffordance

    /**
     * All Action-based Interaction Affordances of the Thing.
     *
     * @return a map of action affordances.
     */
    var actions: MutableMap<String, ThingAction<Any, Any>> // Optional: Map of ActionAffordance

    /**
     * All Event-based Interaction Affordances of the Thing.
     *
     * @return a map of event affordances.
     */
    var events: MutableMap<String, ThingEvent<Any>> // Optional: Map of EventAffordance

    /**
     * Provides Web links to arbitrary resources that relate to the specified Thing Description.
     *
     * @return an array of links.
     */
    var links: List<Link>? // Optional: Array of Link

    /**
     * Set of form hypermedia controls that describe how an operation can be performed.
     *
     * @return an array of forms.
     */
    var forms: List<Form>? // Optional: Array of Form

    /**
     * Set of security definition names, chosen from those defined in securityDefinitions.
     *
     * @return a string or an array of strings representing security definitions, mandatory.
     */
    var security: List<String> // Mandatory: string or Array of string

    /**
     * Set of named security configurations (definitions only).
     *
     * @return a map of security schemes, mandatory.
     */
    var securityDefinitions: MutableMap<String, SecurityScheme> // Mandatory: Map of SecurityScheme

    /**
     * Indicates the WoT Profile mechanisms followed by this Thing Description and the corresponding Thing implementation.
     *
     * @return an optional profile URI or an array of URIs.
     */
    var profile: List<String>? // Optional: anyURI or Array of anyURI

    /**
     * Set of named data schemas to be used in a schema name-value pair.
     *
     * @return a map of data schemas, optional.
     */
    var schemaDefinitions: MutableMap<String, DataSchema<Any>>? // Optional: Map of DataSchema

    /**
     * Define URI template variables according to RFC6570 as collection based on DataSchema declarations.
     *
     * @return a map of URI variables.
     */
    var uriVariables: MutableMap<String, DataSchema<Any>>? // Optional: Map of DataSchema
}

/**
 * Data class representing version information of a Thing Description (TD) and its underlying Thing Model (TM).
 *
 * @property instance Provides a version indicator of this TD. This field is mandatory.
 * @property model Provides a version indicator of the underlying TM. This field is optional.
 */
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

