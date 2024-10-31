package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.thing.EventAffordance
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.slf4j.LoggerFactory


class ExposedThingEvent<T>(event: ThingEvent<T>) : EventAffordance<T, Any, Any> by event {

    private val state : EventState<T> = EventState()

    fun getState(): EventState<T> {
        return state
    }

    suspend fun emit(data: T) {
        log.debug("Event '{}' has been emitted", title)
        state.emit(data)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExposedThingEvent::class.java)
    }

    class EventState<T> internal constructor(private val _flow: MutableSharedFlow<T>) {

        constructor() : this(MutableSharedFlow())

        val flow: Flow<T>
            get() = _flow

        suspend fun emit(value: T) {
            _flow.emit(value)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ThingEvent<T>(
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,

    @JsonProperty("@type")
    @JsonInclude(NON_NULL)
    override var objectType: String? = null,

    @JsonInclude(NON_NULL)
    override var data: DataSchema<T>? = null,

    @JsonInclude(NON_EMPTY)
    override var description: String? = null,

    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var uriVariables: MutableMap<String, Map<String, Any>>? = null,

    @JsonInclude(NON_EMPTY)
    override var forms: MutableList<Form>? = null,
    @JsonInclude(NON_EMPTY)
    override var subscription: DataSchema<Any>? = null,
    @JsonInclude(NON_EMPTY)
    override var cancellation: DataSchema<Any>? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null

) : EventAffordance<T, Any, Any>
