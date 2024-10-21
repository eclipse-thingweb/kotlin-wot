package ai.ancf.lmos.wot.schema

import ai.ancf.lmos.wot.schema.AbstractDataSchema

/**
 * Describes data of type [null](https://www.w3.org/TR/wot-thing-description/#nullschema).
 */
class NullSchema : AbstractDataSchema<Any?>() {
    override val type: String?
        get() = TYPE
    override val classType: Class<T>
        get() = CLASS_TYPE

    override fun toString(): String {
        return "NullSchema{}"
    }

    companion object {
        const val TYPE = "null"
        val CLASS_TYPE = Any::class.java
    }
}
