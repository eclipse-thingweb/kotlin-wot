package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory
import java.util.*


class ExposedThingEvent<T> internal constructor(
    private val name: String,
    @JsonIgnore private val state: EventState<T>,
    objectType: String? = null,
    data: DataSchema<T>? = null,
    type: String? = null,
    description: String? = null,
    descriptions: Map<String, String>? = null,
    uriVariables: Map<String, Map<String, Any?>>? = null
) : ThingEvent<T>(objectType, data, type) {

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
