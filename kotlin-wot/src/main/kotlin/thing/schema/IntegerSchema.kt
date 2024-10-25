package ai.ancf.lmos.wot.schema

/**
 * Describes data of type [integer](https://www.w3.org/TR/wot-thing-description/#integerschema).
 */
class IntegerSchema : AbstractDataSchema() {
    override val type: String
        get() = TYPE

    override val classType: Class<Int>
        get() = Int::class.javaObjectType

    override fun toString(): String {
        return "IntegerSchema{}"
    }

    companion object {
        const val TYPE = "integer"
        val CLASS_TYPE: Class<Int> = Int::class.java
    }
}
