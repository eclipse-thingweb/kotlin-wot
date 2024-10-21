package ai.ancf.lmos.wot.schema

import ai.ancf.lmos.wot.schema.AbstractDataSchema
import ai.ancf.lmos.wot.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

/**
 * Describes data of type [object](https://www.w3.org/TR/wot-thing-description/#objectschema).
 */
class ObjectSchema @JvmOverloads constructor(
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val properties: Map<String?, DataSchema<*>?> = HashMap(),
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val required: List<String?> = ArrayList()
) : AbstractDataSchema<Map<*, *>?>() {

    override val type: String?
        get() = TYPE

    override fun toString(): String {
        return "ObjectSchema{}"
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), properties, required)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is ObjectSchema) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        val that = o
        return properties == that.properties && required == that.required
    }

    companion object {
        const val TYPE = "object"
        val classType: Class<Map<*, *>> = MutableMap::class.java
            get() = Companion.field
    }
}
