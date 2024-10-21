package ai.ancf.lmos.wot.schema

import ai.ancf.lmos.wot.schema.AbstractDataSchema

/**
 * Describes data of type [string](https://www.w3.org/TR/wot-thing-description/#stringschema).
 */
class StringSchema : AbstractDataSchema<String?>() {
    override val type: String?
        get() = TYPE

    override fun toString(): String {
        return "StringSchema{}"
    }

    companion object {
        const val TYPE = "string"
        val classType = String::class.java
            get() = Companion.field
    }
}
