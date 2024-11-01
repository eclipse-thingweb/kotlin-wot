package ai.ancf.lmos.wot.thing.schema

/**
 * Describes data of type [number](https://www.w3.org/TR/wot-thing-description/#numberschema).
 */

abstract class NumberSchema : AbstractDataSchema<Number>() {

    override val classType: Class<Number>
        get() = Number::class.java

    override fun toString(): String {
        return "NumberSchema{}"
    }

    companion object {
        const val TYPE = "number"
        val CLASS_TYPE: Class<Number> =  Number::class.java
    }
}