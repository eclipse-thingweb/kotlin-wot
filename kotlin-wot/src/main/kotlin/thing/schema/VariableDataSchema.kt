package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.schema.NullSchema


/**
 * Describes data whose type is determined at runtime.
 */
class VariableDataSchema(override val type: String) : AbstractDataSchema() {

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
