package ai.ancf.lmos.wot.thing.schema

/**
 * Describes data of type [boolean](https://www.w3.org/TR/wot-thing-description/#booleanschema).
 */
abstract class BooleanSchema : AbstractDataSchema<Boolean>() {

    override val classType: Class<Boolean>
        get() = Boolean::class.java

    override fun toString(): String {
        return "BooleanSchema{}"
    }

    companion object {
        const val TYPE = "boolean"
        val CLASS_TYPE: Class<Boolean> = Boolean::class.java
    }
}