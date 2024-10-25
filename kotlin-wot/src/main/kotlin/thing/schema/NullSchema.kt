package ai.ancf.lmos.wot.schema

/**
 * Describes data of type [null](https://www.w3.org/TR/wot-thing-description/#nullschema).
 */
class NullSchema : AbstractDataSchema() {
    override val type: String
        get() = TYPE

    override val classType: Class<Any>
        get() = Any::class.java

    override fun toString(): String {
        return "NullSchema{}"
    }

    companion object {
        const val TYPE = "null"
        val CLASS_TYPE: Class<Any> = Any::class.java
    }
}


