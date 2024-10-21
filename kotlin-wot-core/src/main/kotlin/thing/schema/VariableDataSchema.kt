package ai.ancf.lmos.wot.schema

import ai.ancf.lmos.wot.schema.*
import io.github.sanecity.ObjectBuilder

/**
 * Describes data whose type is determined at runtime.
 */
class VariableDataSchema : AbstractDataSchema<Any?>() {
    override var type: String? = null
        private set

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return super.equals(obj)
    }

    override val classType: Class<T>
        get() = when (type) {
            ArraySchema.Companion.TYPE -> ArraySchema.Companion.CLASS_TYPE
            BooleanSchema.Companion.TYPE -> BooleanSchema.Companion.CLASS_TYPE
            IntegerSchema.Companion.TYPE -> IntegerSchema.Companion.CLASS_TYPE
            NullSchema.Companion.TYPE -> NullSchema.Companion.CLASS_TYPE
            NumberSchema.Companion.TYPE -> NumberSchema.Companion.CLASS_TYPE
            ObjectSchema.Companion.TYPE -> ObjectSchema.Companion.CLASS_TYPE
            else -> StringSchema.Companion.CLASS_TYPE
        }

    override fun toString(): String {
        return "VariableDataSchema{" +
                "type='" + type + '\'' +
                '}'
    }

    /**
     * Allows building new [VariableDataSchema] objects.
     */
    class Builder : ObjectBuilder<VariableDataSchema?> {
        private var type: String? = null
        fun setType(type: String?): Builder {
            this.type = type
            return this
        }

        fun build(): VariableDataSchema {
            val schema = VariableDataSchema()
            schema.type = type
            return schema
        }
    }
}
