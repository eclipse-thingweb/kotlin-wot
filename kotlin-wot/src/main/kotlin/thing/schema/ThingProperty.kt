package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.form.Form
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
    data class StringProperty(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<String>, StringSchema()

    data class IntProperty(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<Int>, IntegerSchema()

    data class BooleanProperty(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<Boolean>, BooleanSchema()

    data class NumberProperty(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<Number>, NumberSchema()

    data class ArrayProperty<I>(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<List<Any>>, ArraySchema<I>()

    data class NullProperty(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<Any>, NullSchema()

    data class ObjectProperty(
        override var forms: MutableList<Form>? = mutableListOf(),
        override var uriVariables: MutableMap<String, DataSchema<Any>>? = mutableMapOf(),
        override var observable: Boolean = false
    ) : ThingProperty<Map<Any, Any>>, ObjectSchema()

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