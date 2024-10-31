package ai.ancf.lmos.wot.thing

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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class ExposedThing(thing: Thing) : ThingDescription by thing {

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
     * Returns a [Map] with property names as map key and property values as map value.
     *
     * @return
     */
    suspend fun readProperties(): Map<String, Any> {
        val values: MutableMap<String, Any> = HashMap()
        _properties.forEach { (name, property) ->
            values[name] = property.read() as Any
        }
        return values
    }

    /**
     * Writes the transferred `values` to the respective properties and returns the new
     * value of the respective properties.
     *
     * @param values
     * @return
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

    companion object {
        private val log = LoggerFactory.getLogger(ExposedThing::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Thing (
    override val id: String = "urn:uuid:" + UUID.randomUUID().toString(),
    @JsonProperty("@type") @JsonInclude(NON_NULL) override var objectType: Type? = null,
    @JsonProperty("@context") @JsonInclude(NON_NULL) override var objectContext: Context? = null,
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
    @JsonInclude(NON_EMPTY) override var links: List<String>? = null,
    @JsonInclude(NON_EMPTY) override var profile: List<String>? = null,
    @JsonInclude(NON_EMPTY) override var schemaDefinitions: MutableMap<String, DataSchema<Any>>? = null,
    @JsonInclude(NON_EMPTY) override var uriVariables: MutableMap<String, DataSchema<Any>>? = null
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

    companion object {
        private val log = LoggerFactory.getLogger(Thing::class.java)
        private val JSON_MAPPER = ObjectMapper().registerKotlinModule()

        fun randomId(): String {
            return "urn:uuid:${UUID.randomUUID()}"
        }

        fun fromJson(json: String?): Thing? {
            return try {
                JSON_MAPPER.readValue(json, Thing::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                null
            }
        }

        fun fromJson(file: File?): Thing? {
            return try {
                JSON_MAPPER.readValue(file, Thing::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                null
            }
        }

        fun fromMap(map: Map<*, *>): Thing {
            return JSON_MAPPER.convertValue(map, Thing::class.java)
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
    var links: List<String>? // Optional: Array of Link

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