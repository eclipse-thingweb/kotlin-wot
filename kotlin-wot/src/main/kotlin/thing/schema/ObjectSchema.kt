package ai.ancf.lmos.wot.thing.schema

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

/**
 * Describes data of type [object](https://www.w3.org/TR/wot-thing-description/#objectschema).
 */
abstract class ObjectSchema(
    @JsonInclude(JsonInclude.Include.NON_EMPTY) private val properties: Map<String?, DataSchema<*>?> = HashMap(),
    @JsonInclude(JsonInclude.Include.NON_EMPTY) private val required: List<String?> = ArrayList()
) : AbstractDataSchema<Map<*, *>?>() {

    override val type: String
        get() = ObjectSchema.TYPE

    override val classType: Class<String>
        get() = String::class.java

    override fun toString(): String {
        return "ObjectSchema{}"
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), properties, required)
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj !is ObjectSchema) {
            return false
        }
        if (!super.equals(obj)) {
            return false
        }
        val that = obj
        return properties == that.properties && required == that.required
    }

    companion object {
        const val TYPE = "object"
        val CLASS_TYPE: Class<Map<*, *>> = Map::class.java
    }
}
