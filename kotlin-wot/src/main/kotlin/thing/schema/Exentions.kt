package ai.ancf.lmos.wot.thing.schema

// Extension function for creating an InteractionInput.Value from a string
fun String.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.StringValue(this))
}

fun Boolean.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.BooleanValue(this))
}

fun Number.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.NumberValue(this))
}
fun Int.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.IntegerValue(this))
}

fun List<DataSchemaValue>.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(DataSchemaValue.ArrayValue(this))
}

