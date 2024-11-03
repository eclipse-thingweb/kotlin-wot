package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.EventAffordance
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory

@Serializable
data class ExposedThingEvent<T, S, C>(private val event: ThingEvent<T, S, C> = ThingEvent()) : EventAffordance<T, S, C> by event {

    @Transient @JsonIgnore private val state : EventState<T> = EventState()

    fun getState(): EventState<T> {
        return state
    }

    suspend fun emit(data: T) {
        log.debug("Event '{}' has been emitted", title)
        state.emit(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExposedThingEvent<*, *, *>

        return event == other.event
    }

    override fun hashCode(): Int {
        return event.hashCode()
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
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class ThingEvent<T, S, C>(
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,

    @SerialName("@type")
    @JsonProperty("@type")
    @JsonInclude(NON_NULL)
    override var objectType: Type? = null,

    @JsonInclude(NON_NULL)
    override var data: DataSchema<T>? = null,

    @JsonInclude(NON_EMPTY)
    override var description: String? = null,

    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var uriVariables: MutableMap<String, DataSchema<@Contextual Any>>? = null,

    @JsonInclude(NON_EMPTY)
    override var forms: MutableList<Form>? = null,

    @JsonInclude(NON_EMPTY)
    override var subscription: DataSchema<@Contextual S>? = null,

    @JsonInclude(NON_EMPTY)
    override var cancellation: DataSchema<@Contextual C>? = null,

    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null

) : EventAffordance<T, S, C>
