package ai.ancf.lmos.wot.thing.schema

/**
 * Describes data of type [string](https://www.w3.org/TR/wot-thing-description/#stringschema).
 */
class StringSchema : AbstractDataSchema() {
    override val type: String
        get() = TYPE

    override val classType: Class<String>
        get() = String::class.java

    override fun toString(): String {
        return "StringSchema{}"
    }

    companion object {
        const val TYPE = "string"
        val CLASS_TYPE: Class<String> = String::class.java
    }
}
