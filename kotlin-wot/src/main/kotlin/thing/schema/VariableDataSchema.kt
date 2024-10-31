package ai.ancf.lmos.wot.thing.schema


/**
 * Describes data whose type is determined at runtime.
 */
abstract class VariableDataSchema(override val type: String) : AbstractDataSchema<Any>() {

    override val classType: Class<*>
        get() = when (type) {
            ArraySchema.TYPE -> ArraySchema.CLASS_TYPE
            BooleanSchema.TYPE -> BooleanSchema.CLASS_TYPE
            IntegerSchema.TYPE -> IntegerSchema.CLASS_TYPE
            NullSchema.TYPE -> NullSchema.CLASS_TYPE
            NumberSchema.TYPE -> NumberSchema.CLASS_TYPE
            else -> StringSchema.CLASS_TYPE
        }

    override fun toString(): String {
        return "VariableDataSchema{" +
                "type='" + type + '\'' +
                '}'
    }
}
