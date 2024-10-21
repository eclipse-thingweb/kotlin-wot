package ai.ancf.lmos.wot.schema
/**
 * Describes data of type [Array](https://www.w3.org/TR/wot-thing-description/#arrayschema).
 */

class ArraySchema : AbstractDataSchema<List<*>>() {
    override val type: String
        get() = TYPE

    override val classType: Class<List<*>>
        get() = List::class.java

    override fun toString(): String {
        return "ArraySchema{}"
    }

    companion object {
        const val TYPE = "array"
    }
}
