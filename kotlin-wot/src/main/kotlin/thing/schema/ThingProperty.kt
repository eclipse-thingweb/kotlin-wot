package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import ai.ancf.lmos.wot.thing.schema.ThingProperty.*
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = StringProperty::class, name = "string") ,
    JsonSubTypes.Type(value = IntProperty::class, name = "integer"),
    JsonSubTypes.Type(value = BooleanProperty::class, name = "boolean"),
    JsonSubTypes.Type(value = NumberProperty::class, name = "number"),
    JsonSubTypes.Type(value = ArrayProperty::class, name = "array"),
    JsonSubTypes.Type(value = ObjectProperty::class, name = "object"),
    JsonSubTypes.Type(value = NullProperty::class, name = "null")
)
sealed interface ThingProperty<T> : PropertyAffordance<T>{

    var readHandler: PropertyReadHandler<T>?
    var writeHandler: PropertyWriteHandler<T>?

    suspend fun read(): T? {
        log.debug("'{}' calls registered readHandler for Property '{}'", thing.id, title)
        return try {
            readHandler?.handle()?.also { state.setValue(it) } ?: state.value
        } catch (e: Exception) {
            ExposedThingProperty.log.error("Error while reading property '{}': {}", title, e.message)
            throw e
        }
    }

    suspend fun write(value: T): T? {
        ExposedThingProperty.log.debug("'{}' calls registered writeHandler for Property '{}'", thing.id, title)
        return try {
            if (state.writeHandler != null) {
                val customValue = state.writeHandler.handle(value)
                state.setValue(customValue)
                ExposedThingProperty.log.debug(
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

    data class StringProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var readHandler: PropertyReadHandler<String>? = null,
        override var writeHandler: PropertyWriteHandler<String>? = null
    ) : ThingProperty<String>, StringSchema()

    data class IntProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var readHandler: PropertyReadHandler<Int>? = null,
        override var writeHandler: PropertyWriteHandler<Int>? = null
    ) : ThingProperty<Int>, IntegerSchema()

    data class BooleanProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var readHandler: PropertyReadHandler<Boolean>? = null,
        override var writeHandler: PropertyWriteHandler<Boolean>? = null
    ) : ThingProperty<Boolean>, BooleanSchema()

    data class NumberProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var readHandler: PropertyReadHandler<Number>? = null,
        override var writeHandler: PropertyWriteHandler<Number>? = null
    ) : ThingProperty<Number>, NumberSchema()

    data class ArrayProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var items: List<DataSchema<Any>>? = mutableListOf(),
        override var readHandler: PropertyReadHandler<List<*>>? = null,
        override var writeHandler: PropertyWriteHandler<List<*>>? = null
    ) : ThingProperty<List<*>>, ArraySchema<Any>(items = items)

    data class NullProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var readHandler: PropertyReadHandler<Any>? = null,
        override var writeHandler: PropertyWriteHandler<Any>? = null
    ) : ThingProperty<Any>, NullSchema()

    data class ObjectProperty(
        override var forms: MutableList<Form> = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false,
        override var readHandler: PropertyReadHandler<Map<*, *>>? = null,
        override var writeHandler: PropertyWriteHandler<Map<*, *>>? = null
    ) : ThingProperty<Map<*, *>>, ObjectSchema()
}

fun stringProperty(initializer: StringProperty.() -> Unit): StringProperty {
    return StringProperty().apply(initializer)
}
fun intProperty(initializer: IntProperty.() -> Unit): IntProperty {
    return IntProperty().apply(initializer)
}
fun booleanProperty(initializer: BooleanProperty.() -> Unit): BooleanProperty {
    return BooleanProperty().apply(initializer)
}

fun numberProperty(initializer: NumberProperty.() -> Unit): NumberProperty {
    return NumberProperty().apply(initializer)
}

fun nullProperty(initializer: NullProperty.() -> Unit): NullProperty {
    return NullProperty().apply(initializer)
}

fun objectProperty(initializer: ObjectProperty.() -> Unit): ObjectProperty {
    return ObjectProperty().apply(initializer)
}

fun <I> arrayProperty(items: List<DataSchema<I>>, initializer: ArrayProperty.() -> Unit): ArrayProperty {
    return ArrayProperty().apply { initializer() }
}