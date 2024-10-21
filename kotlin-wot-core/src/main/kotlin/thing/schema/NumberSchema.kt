package ai.ancf.lmos.wot.schema

/**
 * Describes data of type [number](https://www.w3.org/TR/wot-thing-description/#numberschema).
 */

class NumberSchema : AbstractDataSchema<Number>() {
    override val type: String
        get() = TYPE

    override val classType: Class<Number>
        get() = Number::class.java

    override fun toString(): String {
        return "NumberSchema{}"
    }

    companion object {
        const val TYPE = "number"
    }
}