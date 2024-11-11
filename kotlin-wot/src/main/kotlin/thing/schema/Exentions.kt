package ai.ancf.lmos.wot.thing.schema

// Extension function for creating an InteractionInput.Value from a string
fun String.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.StringValue(this))
}

fun MutableMap<*,*>.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.ObjectValue(this))
}

fun MutableList<*>.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.ArrayValue(this))
}

fun String.toDataSchemeValue(): DataSchemaValue {
    return DataSchemaValue.StringValue(this)
}

fun Boolean.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.BooleanValue(this))
}

fun Boolean.toDataSchemeValue(): DataSchemaValue {
    return DataSchemaValue.BooleanValue(this)
}


fun Number.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.NumberValue(this))
}
fun Number.toDataSchemeValue(): DataSchemaValue {
    return DataSchemaValue.NumberValue(this)
}

fun Int.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.IntegerValue(this))
}

fun Int.toDataSchemeValue(): DataSchemaValue {
    return DataSchemaValue.IntegerValue(this)
}

fun List<*>.toDataSchemeValue(): DataSchemaValue {
    return DataSchemaValue.ArrayValue(this)
}
