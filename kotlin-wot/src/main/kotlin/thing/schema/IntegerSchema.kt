package ai.ancf.lmos.wot.thing.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

/**
 * Data class representing an integer schema, with optional constraints such as minimum, maximum, etc.
 *
 * @property minimum Specifies a minimum numeric value, representing an inclusive lower limit.
 * @property exclusiveMinimum Specifies a minimum numeric value, representing an exclusive lower limit.
 * @property maximum Specifies a maximum numeric value, representing an inclusive upper limit.
 * @property exclusiveMaximum Specifies a maximum numeric value, representing an exclusive upper limit.
 * @property multipleOf Specifies the multipleOf value. The value must be strictly greater than 0.
 */
data class IntegerSchema(
    @JsonInclude(NON_EMPTY)
    override val contextType: String? = null,
    @JsonInclude(NON_EMPTY)
    override val title: String? = null,
    @JsonInclude(NON_EMPTY)
    override val titles: Map<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override val description: String? = null,
    @JsonInclude(NON_EMPTY)
    override val descriptions: Map<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override val const: Any? = null,
    @JsonInclude(NON_EMPTY)
    override val default: Any? = null,
    @JsonInclude(NON_EMPTY)
    override val unit: String? = null,
    @JsonInclude(NON_EMPTY)
    override val oneOf: List<DataSchema<Any>>? = null,
    @JsonInclude(NON_EMPTY)
    override val enum: List<Any>? = null,
    @JsonInclude(NON_NULL)
    override val readOnly: Boolean? = false,
    @JsonInclude(NON_NULL)
    override val writeOnly: Boolean? = false,
    @JsonInclude(NON_NULL)
    override val format: String? = null,
    @JsonInclude(NON_NULL)
    val minimum: Int? = null,
    @JsonInclude(NON_NULL)
    val exclusiveMinimum: Int? = null,
    @JsonInclude(NON_NULL)
    val maximum: Int? = null,
    @JsonInclude(NON_NULL)
    val exclusiveMaximum: Int? = null,
    @JsonInclude(NON_NULL)
    val multipleOf: Int? = null
) : AbstractDataSchema<Int>() {

    override val type: String
        get() = TYPE

    @get:JsonIgnore
    override val classType: Class<Number>
        get() = Number::class.java

    override fun toString(): String {
        return "NumberSchema{}"
    }

    companion object {
        const val TYPE = "integer"
        val CLASS_TYPE: Class<Int> = Int::class.java
    }
}