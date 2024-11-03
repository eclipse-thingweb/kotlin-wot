package ai.ancf.lmos.wot.thing.property


import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.schema.PropertyAffordance
import ai.ancf.lmos.wot.thing.schema.ThingProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class ExposedThingProperty<T>(
    private val property: ThingProperty<T>,
    @JsonIgnore
    private val thing: Thing = Thing(),
    @Transient private val state: PropertyState<T> = PropertyState()
) : PropertyAffordance<T> by property {

    suspend fun read(): T? {
        log.debug("'{}' calls registered readHandler for Property '{}'", thing.id, title)
        return try {
            state.readHandler?.invoke()?.also { state.setValue(it) } ?: state.value
        } catch (e: Exception) {
            log.error("Error while reading property '{}': {}", title, e.message)
            throw e
        }
    }

    suspend fun write(value: T): T? {
        log.debug("'{}' calls registered writeHandler for Property '{}'", thing.id, title)
        return try {
            if (state.writeHandler != null) {
                val customValue = state.writeHandler.invoke(value)
                state.setValue(customValue)
                log.debug(
                    "'{}' write handler for Property '{}' sets custom value '{}'",
                    thing.id,
                    title,
                    customValue
                )
                customValue
            } else {
                state.setValue(value)
                value
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExposedThingProperty<*>

        if (property != other.property) return false
        if (thing != other.thing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = property.hashCode()
        result = 31 * result + thing.hashCode()
        return result
    }

    data class PropertyState<T>(
        private val _flow: MutableStateFlow<T?> = MutableStateFlow(null),
        val readHandler: (suspend () -> T?)? = null,
        val writeHandler: (suspend (T) -> T?)? = null
    ) {

        // Getter for the current value
        val value: T? get() = _flow.value

        // Emit value to the flow
        private suspend fun emit(value: T) {
            _flow.emit(value)
        }

        // Update the value
        suspend fun setValue(newValue: T?) {
            _flow.value = newValue
            newValue?.let { emit(it) }
        }
    }



    companion object {
        private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(ExposedThingProperty::class.java)
    }
}


