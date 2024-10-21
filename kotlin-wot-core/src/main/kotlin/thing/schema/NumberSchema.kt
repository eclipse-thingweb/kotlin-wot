package ai.ancf.lmos.wot.schema

import ai.ancf.lmos.wot.schema.AbstractDataSchema

/**
 * Describes data of type [number](https://www.w3.org/TR/wot-thing-description/#numberschema).
 */
class NumberSchema : AbstractDataSchema<Number?>() {
    override val type: String?
        get() = TYPE

    override fun toString(): String {
        return "NumberSchema{}"
    }

    companion object {
        const val TYPE = "number"
        val classType = Number::class.java
            get() = Companion.field
    }
}
