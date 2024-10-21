package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ThingProperty
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
open class Thing<
        P : ThingProperty<Any?>,
        A : ThingAction<Any?, Any?>,
        E : ThingEvent<Any?>
        >(
    @JsonProperty("@type") @JsonInclude(JsonInclude.Include.NON_NULL) val objectType: Type? = null,
    @JsonProperty("@context") @JsonInclude(JsonInclude.Include.NON_NULL) val objectContext: Context? = null,
    val id: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val title: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val titles: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val description: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val descriptions: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val properties: Map<String, P> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val actions: Map<String, A> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val events: Map<String, E> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val forms: List<Form> = emptyList(),
    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY]) @JsonInclude(JsonInclude.Include.NON_EMPTY) val security: List<String> = emptyList(),
    @JsonProperty("securityDefinitions") @JsonInclude(JsonInclude.Include.NON_EMPTY) val securityDefinitions: Map<String, SecurityScheme> = emptyMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) val base: String? = null,
    @get:JsonAnyGetter @JsonAnySetter val metadata: Map<String, Any> = emptyMap()
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Thing<*, *, *> -> false
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

    fun getPropertiesByObjectType(objectType: String?): Map<String, P> {
        return getPropertiesByExpandedObjectType(getExpandedObjectType(objectType))
    }

    fun getPropertiesByExpandedObjectType(objectType: String?): Map<String, P> {
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
        private val JSON_MAPPER = ObjectMapper()

        fun randomId(): String {
            return "urn:uuid:${UUID.randomUUID()}"
        }

        fun fromJson(json: String?): Thing<*, *, *>? {
            return try {
                JSON_MAPPER.readValue(json, Thing::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                null
            }
        }

        fun fromJson(file: File?): Thing<*, *, *>? {
            return try {
                JSON_MAPPER.readValue(file, Thing::class.java)
            } catch (e: IOException) {
                log.warn("Unable to read json", e)
                null
            }
        }

        fun fromMap(map: Map<String?, Map<*, *>?>?): Thing<*, *, *> {
            return JSON_MAPPER.convertValue(map, Thing::class.java)
        }
    }
}