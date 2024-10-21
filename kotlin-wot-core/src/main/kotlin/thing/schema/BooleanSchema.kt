package ai.ancf.lmos.wot.schema

/**
 * Describes data of type [boolean](https://www.w3.org/TR/wot-thing-description/#booleanschema).
 */
class BooleanSchema : AbstractDataSchema<Boolean?>() {
    override val type: String?
        get() = TYPE

    override fun toString(): String {
        return "BooleanSchema{}"
    }

    companion object {
        const val TYPE = "boolean"
        val classType = Boolean::class.java
            get() = Companion.field
    }
}
