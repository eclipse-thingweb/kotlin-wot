package ai.ancf.lmos.wot.thing.property


import ai.ancf.lmos.wot.thing.PropertyAffordance
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ExposedThingProperty<T>(
    private val property: ThingProperty<T>,
    @JsonIgnore
    private val thing: Thing,
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
                val customValue = state.writeHandler?.invoke(value)
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

    data class PropertyState<T>(
        private val _flow: MutableStateFlow<T?> = MutableStateFlow(null),
        var readHandler: (suspend () -> T?)? = null,
        var writeHandler: (suspend (T) -> T?)? = null
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
@Serializable

data class ThingProperty<T>(
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,

    @SerialName("@type")
    @JsonProperty("@type")
    @JsonInclude(NON_NULL)
    override var objectType: String? = null,

    @JsonInclude(NON_NULL)
    override var type: String? = "string",

    @JsonInclude(NON_DEFAULT)
    override var observable: Boolean = false,

    @JsonInclude(NON_DEFAULT)
    override var readOnly: Boolean = false,

    @JsonInclude(NON_DEFAULT)
    override var writeOnly: Boolean = false,

    @JsonInclude(NON_EMPTY)
    override var description: String? = null,

    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var forms: MutableList<Form>? = null,

    @JsonInclude(NON_EMPTY)
    override var uriVariables: MutableMap<String, DataSchema<@Contextual Any>>? = null,

    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var const: T?  = null,

    @JsonInclude(NON_EMPTY)
    override var default: T?  = null,

    @JsonInclude(NON_EMPTY)
    override var unit: String?  = null,

    @JsonInclude(NON_EMPTY)
    override var oneOf: List<DataSchema<@Contextual Any>>?  = null,

    @JsonInclude(NON_EMPTY)
    override var enum: List<@Contextual Any>? = null,

    @JsonInclude(NON_EMPTY)
    override var format: String? = null,
    ) : PropertyAffordance<T> {

        /*
    override val classType: Class<*>
        get() = VariableDataSchema(type).classType

         */

}

