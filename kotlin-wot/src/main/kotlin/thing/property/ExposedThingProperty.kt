package ai.ancf.lmos.wot.thing.property


import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty.*
import ai.ancf.lmos.wot.thing.schema.PropertyAffordance
import ai.ancf.lmos.wot.thing.schema.ThingProperty
import ai.ancf.lmos.wot.thing.schema.ThingProperty.*
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ExposedStringProperty::class, name = "string") ,
    JsonSubTypes.Type(value = ExposedIntProperty::class, name = "integer"),
    JsonSubTypes.Type(value = ExposedBooleanProperty::class, name = "boolean"),
    JsonSubTypes.Type(value = ExposedNumberProperty::class, name = "number"),
    JsonSubTypes.Type(value = ExposedArrayProperty::class, name = "array"),
    JsonSubTypes.Type(value = ExposedObjectProperty::class, name = "object"),
    JsonSubTypes.Type(value = ExposedNullProperty::class, name = "null")
)
sealed class ExposedThingProperty<T>(
    private val property: ThingProperty<T>,
    @JsonIgnore
    private val thing: ExposedThing = ExposedThing(),
    private val state: PropertyState<T> = PropertyState()
) : PropertyAffordance<T> by property {

    suspend fun read(): T? {
        log.debug("'{}' calls registered readHandler for Property '{}'", thing.id, title)
        return try {
            state.readHandler?.handle()?.also { state.setValue(it) } ?: state.value
        } catch (e: Exception) {
            log.error("Error while reading property '{}': {}", title, e.message)
            throw e
        }
    }

    suspend fun write(value: T): T? {
        log.debug("'{}' calls registered writeHandler for Property '{}'", thing.id, title)
        return try {
            if (state.writeHandler != null) {
                val customValue = state.writeHandler.handle(value)
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

    override fun toString(): String {
        return "ExposedThingProperty(property=$property)"
    }


    data class PropertyState<T>(
        private val initialValue: T? = null,  // Add initial value parameter
        private val _flow: MutableStateFlow<T?> = MutableStateFlow(initialValue),
        val readHandler: ReadHandler<T>? = null,
        val writeHandler: WriteHandler<T>? = null
    ) {

        // Getter for the current value
        val value: T? get() = _flow.value

        // Public read-only view of the flow
        val flow: StateFlow<T?> get() = _flow

        // Update the value, which automatically emits the new value
        fun setValue(newValue: T?) {
            _flow.value = newValue  // This automatically emits the new value
        }
    }


    class ExposedStringProperty(property: StringProperty = StringProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<String> = PropertyState()) : ExposedThingProperty<String>(property, thing, state)
    class ExposedIntProperty(property: IntProperty = IntProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<Int> = PropertyState()) : ExposedThingProperty<Int>(property, thing, state)
    class ExposedBooleanProperty(property: BooleanProperty = BooleanProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<Boolean> = PropertyState()) : ExposedThingProperty<Boolean>(property, thing, state)
    class ExposedNumberProperty(property: NumberProperty = NumberProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<Number> = PropertyState()) : ExposedThingProperty<Number>(property, thing, state)
    class ExposedNullProperty(property: NullProperty = NullProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<Any> = PropertyState()) : ExposedThingProperty<Any>(property, thing, state)
    class ExposedArrayProperty(property: ArrayProperty = ArrayProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<List<*>> = PropertyState()) : ExposedThingProperty<List<*>>(property, thing, state)
    class ExposedObjectProperty(property: ObjectProperty = ObjectProperty(), thing : ExposedThing = ExposedThing(), state: PropertyState<Map<*, *>> = PropertyState()) : ExposedThingProperty<Map<*, *>>(property, thing, state)

    companion object {
        private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(ExposedThingProperty::class.java)
    }
}

fun exposedStringProperty(initializer: ExposedStringProperty.() -> Unit): ExposedStringProperty {
    return ExposedStringProperty().apply(initializer)
}
fun exposedIntProperty(initializer: ExposedIntProperty.() -> Unit): ExposedIntProperty {
    return ExposedIntProperty().apply(initializer)
}
fun exposedBooleanProperty(initializer: ExposedBooleanProperty.() -> Unit): ExposedBooleanProperty {
    return ExposedBooleanProperty().apply(initializer)
}

fun exposedNumberProperty(initializer: ExposedNumberProperty.() -> Unit): ExposedNumberProperty {
    return ExposedNumberProperty().apply(initializer)
}

fun exposedNullProperty(initializer: ExposedNullProperty.() -> Unit): ExposedNullProperty {
    return ExposedNullProperty().apply(initializer)
}

fun exposedObjectProperty(initializer: ExposedObjectProperty.() -> Unit): ExposedObjectProperty {
    return ExposedObjectProperty().apply(initializer)
}


fun interface ReadHandler<T> {
    suspend fun handle(): T?
}

fun interface WriteHandler<T> {
    suspend fun handle(input: T): T?
}