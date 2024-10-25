package ai.ancf.lmos.wot.thing.schema
/**
 * Describes data of type [Array](https://www.w3.org/TR/wot-thing-description/#arrayschema).
 */

class ArraySchema : AbstractDataSchema() {
    override val type: String
        get() = TYPE

    override val classType: Class<List<*>>
        get() = List::class.java

    override fun toString(): String {
        return "ArraySchema{}"
    }

    companion object {
        const val TYPE = "array"
        val CLASS_TYPE: Class<List<*>> = List::class.java
    }
}
