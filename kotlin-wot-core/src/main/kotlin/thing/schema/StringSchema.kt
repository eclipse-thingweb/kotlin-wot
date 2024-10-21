package ai.ancf.lmos.wot.schema

/**
 * Describes data of type [string](https://www.w3.org/TR/wot-thing-description/#stringschema).
 */
class StringSchema : AbstractDataSchema<String>() {
    override val type: String
        get() = TYPE

    override val classType: Class<String>
        get() = String::class.java

    override fun toString(): String {
        return "StringSchema{}"
    }

    companion object {
        const val TYPE = "string"
    }
}