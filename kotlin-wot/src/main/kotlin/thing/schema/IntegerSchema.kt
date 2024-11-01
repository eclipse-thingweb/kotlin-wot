package ai.ancf.lmos.wot.thing.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

/**
 * Data class representing an integer schema, with optional constraints such as minimum, maximum, etc.
 *
 * @property minimum Specifies a minimum numeric varue, representing an inclusive lower limit.
 * @property exclusiveMinimum Specifies a minimum numeric varue, representing an exclusive lower limit.
 * @property maximum Specifies a maximum numeric varue, representing an inclusive upper limit.
 * @property exclusiveMaximum Specifies a maximum numeric varue, representing an exclusive upper limit.
 * @property multipleOf Specifies the multipleOf varue. The varue must be strictly greater than 0.
 */
data class IntegerSchema(
    @JsonInclude(NON_EMPTY)
    override var objectType: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var type: String? = "integer",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: Int? = null,
    @JsonInclude(NON_EMPTY)
    override var default: Int? = null,
    @JsonInclude(NON_EMPTY)
    override var unit: String? = null,
    @JsonInclude(NON_EMPTY)
    override var oneOf: List<DataSchema<Any>>? = null,
    @JsonInclude(NON_EMPTY)
    override var enum: List<Any>? = null,
    @JsonInclude(NON_NULL)
    override var readOnly: Boolean = false,
    @JsonInclude(NON_NULL)
    override var writeOnly: Boolean = false,
    @JsonInclude(NON_NULL)
    override var format: String? = null,
    @JsonInclude(NON_NULL)
    var minimum: Int? = null,
    @JsonInclude(NON_NULL)
    var exclusiveMinimum: Int? = null,
    @JsonInclude(NON_NULL)
    var maximum: Int? = null,
    @JsonInclude(NON_NULL)
    var exclusiveMaximum: Int? = null,
    @JsonInclude(NON_NULL)
    var multipleOf: Int? = null
) : AbstractDataSchema<Int>() {

    @get:JsonIgnore
    override val classType: Class<Number>
        get() = Number::class.java

    override fun toString(): String {
        return "NumberSchema{}"
    }

    companion object {
        const val TYPE = "integer"
        var CLASS_TYPE: Class<Int> = Int::class.java
    }
}