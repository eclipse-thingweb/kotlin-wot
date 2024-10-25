package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.thing.ThingInteraction
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.VariableSchema
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import java.util.*


class ExposedThingEvent<T>(
    private val name: String,
    @JsonIgnore private val state: EventState<T>,
    objectType: String? = null,
    data: VariableSchema? = null,
    type: String? = null,
    description: String? = null,
    descriptions: Map<String, String>? = null,
    uriVariables: Map<String, Map<String, VariableSchema>>? = null
) : ThingEvent<T>(objectType, data, type, description, descriptions, uriVariables, null) {

    constructor(name: String, event: ThingEvent<T>) : this(
        name,
        EventState<T>(),
        event.objectType,
        event.data,
        event.type,
        event.description,
        event.descriptions,
        event.uriVariables
    )

    fun getState(): EventState<T> {
        return state
    }

    suspend fun emit(data: T) {
        log.debug("Event '{}' has been emitted", name)
        state.emit(data)
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), name, state)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        val that = o as ExposedThingEvent<*>
        return name == that.name && state == that.state
    }

    override fun toString(): String {
        return "ExposedThingEvent{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", data=" + data +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}'
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExposedThingEvent::class.java)
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
sealed class ThingEvent<T>(

    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val objectType: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: VariableSchema? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val type: String? = null,

    override val description: String? = null,

    override val descriptions: Map<String, String>? = null,

    override val uriVariables: Map<String, Map<String, VariableSchema>>? = null,

    override val forms: MutableList<Form>?

) : ThingInteraction<ThingEvent<T>?>() {

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), objectType, data, type)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ThingEvent<*>) return false
        if (!super.equals(o)) return false

        return objectType == o.objectType && data == o.data && type == o.type
    }

    override fun toString(): String {
        return "ThingEvent(objectType=$objectType, data=$data, type=$type, " +
                "description='$description', descriptions=$descriptions, " +
                "forms=$forms, uriVariables=$uriVariables)"
    }
}
