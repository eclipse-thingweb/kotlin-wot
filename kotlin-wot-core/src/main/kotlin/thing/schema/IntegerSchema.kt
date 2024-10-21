package ai.ancf.lmos.wot.schema

import ai.ancf.lmos.wot.schema.AbstractDataSchema

/**
 * Describes data of type [integer](https://www.w3.org/TR/wot-thing-description/#integerschema).
 */
class IntegerSchema : AbstractDataSchema<Int?>() {
    override val type: String?
        get() = TYPE

    override fun toString(): String {
        return "IntegerSchema{}"
    }

    companion object {
        const val TYPE = "integer"
        val classType = Int::class.java
            get() = Companion.field
    }
}
