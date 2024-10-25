package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ExposedThingAction
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ExposedThingEvent
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import ai.ancf.lmos.wot.thing.property.ThingProperty
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
import thing.schema.VariableSchema
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
@JsonIgnoreProperties(ignoreUnknown = true)
class ExposedThing(
    @JsonProperty("@type") @JsonInclude(JsonInclude.Include.NON_NULL) objectType: Type? = null,
    @JsonProperty("@context") @JsonInclude(JsonInclude.Include.NON_NULL) objectContext: Context? = null,
    id: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) title: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) titles: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) description: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) descriptions: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) forms: List<Form>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) security: List<String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) securityDefinitions: Map<String, SecurityScheme>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) base: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) metadata: Map<String, VariableSchema>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) properties: Map<String, ExposedThingProperty<VariableSchema>> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) actions: Map<String, ExposedThingAction<VariableSchema, VariableSchema>> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) events: Map<String, ExposedThingEvent<VariableSchema>> = emptyMap()
) : Thing(
    id,
    objectType,
    objectContext,
    title,
    titles,
    description,
    descriptions,
    properties,
    actions,
    events,
    forms,
    security,
    securityDefinitions,
    base,
    metadata
) {


    // Constructor accepting a Thing instance
    constructor(thing: Thing) :
            this(
                thing.objectType,
                thing.objectContext,
                thing.id,
                thing.title,
                thing.titles,
                thing.description,
                thing.descriptions,
                thing.forms,
                thing.security,
                thing.securityDefinitions,
                thing.base,
                thing.metadata,
                // Create properties, actions, and events from the given Thing instance
                thing.properties.mapValues { (name, property) -> ExposedThingProperty(name, property, thing) },
                thing.actions.mapValues { (name, action) -> ExposedThingAction(name, action, thing) },
                thing.events.mapValues { (name, event) -> ExposedThingEvent(name, event) }
            )


    /**
     * Returns a [Map] with property names as map key and property values as map value.
     *
     * @return
     */
    suspend fun readProperties(): CompletableFuture<Map<String, Any>> {
        val futures: MutableList<CompletableFuture<*>> = ArrayList()
        val values: MutableMap<String, VariableSchema> = HashMap()
        properties.forEach { (name, property) ->
            property as ExposedThingProperty
            val readFuture: CompletableFuture<VariableSchema> = property.read()
            val putValuesFuture = readFuture.thenApply { value: Any? -> values[name] = value as VariableSchema }
            futures.add(putValuesFuture)
        }

        // wait until all properties have been read
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { values }
    }

    /**
     * Writes the transferred `values` to the respective properties and returns the new
     * value of the respective properties.
     *
     * @param values
     * @return
     */
    suspend fun writeProperties(values: Map<String, VariableSchema>): CompletableFuture<Map<String, VariableSchema>> {
        val futures: MutableList<CompletableFuture<*>> = ArrayList()
        val returnValues: MutableMap<String, VariableSchema> = HashMap()
        values.forEach { (name, value) ->
            val property: ExposedThingProperty<VariableSchema>? = properties[name] as ExposedThingProperty
            if (property != null) {
                val future: CompletableFuture<VariableSchema> = property.write(value)
                futures.add(future)
                future.whenComplete { _, _ -> returnValues[name] = value }
            }
        }

        // wait until all properties have been written
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { returnValues }
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode())
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        if (!super.equals(o)) return false
        val that = o as ExposedThing
        return id == that.id
    }

    override fun toString(): String {
        return "ExposedThing{" +
                "objectType='$objectType'" +
                ", objectContext=$objectContext" +
                ", id='$id'" +
                ", title='$title'" +
                ", titles=$titles" +
                ", description='$description'" +
                ", descriptions=$descriptions" +
                ", properties=$properties" +
                ", actions=$actions" +
                ", events=$events" +
                ", forms=$forms" +
                ", security=$security" +
                ", securityDefinitions=$securityDefinitions" +
                ", base='$base'" +
                '}'
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExposedThing::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
open class Thing (
    val id: String,
    @JsonProperty("@type") @JsonInclude(JsonInclude.Include.NON_NULL) val objectType: Type? = null,
    @JsonProperty("@context") @JsonInclude(JsonInclude.Include.NON_NULL) val objectContext: Context? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val title: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val titles: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val description: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val descriptions: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val properties: Map<String, ThingProperty<VariableSchema>> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val actions: Map<String, ThingAction<VariableSchema, VariableSchema>> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val events: Map<String, ThingEvent<VariableSchema>> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val forms: List<Form>? = emptyList(),
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) @JsonInclude(JsonInclude.Include.NON_EMPTY) val security: List<String>? = emptyList(),
    @JsonProperty("securityDefinitions") @JsonInclude(JsonInclude.Include.NON_EMPTY) val securityDefinitions: Map<String, SecurityScheme>? = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val base: String? = null,
    @get:JsonAnyGetter @JsonAnySetter val metadata: Map<String, VariableSchema>? = emptyMap()
) {
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

    @JvmOverloads
    fun toJson(prettyPrint: Boolean = false): String? {
        return try {
            if (prettyPrint) {
                JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)
            } else {
                JSON_MAPPER.writeValueAsString(this)
            }
        } catch (e: JsonMappingException) {
            log.warn("Unable to create json", e)
            null
        }
    }

    fun getPropertiesByObjectType(objectType: String?): Map<String, ThingProperty<VariableSchema>> {
        return getPropertiesByExpandedObjectType(getExpandedObjectType(objectType))
    }

    fun getPropertiesByExpandedObjectType(objectType: String?): Map<String, ThingProperty<VariableSchema>> {
        return properties.filter { (key, property) ->
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

        return objectContext.getUrl(prefix)?.let { "$it$suffix" } ?: objectType
    }

    override fun toString(): String {
        return "Thing(objectType=$objectType, objectContext=$objectContext, id='$id', title=$title, titles=$titles, description=$description, descriptions=$descriptions, properties=$properties, actions=$actions, events=$events, forms=$forms, security=$security, securityDefinitions=$securityDefinitions, base='$base', metadata=$metadata)"
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