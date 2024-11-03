package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Data class representing an integer schema, with optional constraints such as minimum, maximum, etc.
 *
 * @property minimum Specifies a minimum numeric varue, representing an inclusive lower limit.
 * @property exclusiveMinimum Specifies a minimum numeric varue, representing an exclusive lower limit.
 * @property maximum Specifies a maximum numeric varue, representing an inclusive upper limit.
 * @property exclusiveMaximum Specifies a maximum numeric varue, representing an exclusive upper limit.
 * @property multipleOf Specifies the multipleOf varue. The varue must be strictly greater than 0.
 */
@JsonTypeName("integer")
open class IntegerSchema(
    @get:JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    @get:JsonInclude(NON_EMPTY)
    override var type: String? = "integer",
    @get:JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @get:JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @get:JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @get:JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @get:JsonInclude(NON_EMPTY)
    override var const: Int? = null,
    @get:JsonInclude(NON_EMPTY)
    override var default: Int? = null,
    @get:JsonInclude(NON_EMPTY)
    override var unit: String? = null,
    @get:JsonInclude(NON_EMPTY)
    override var oneOf: List<DataSchema<Any>>? = null,
    @get:JsonInclude(NON_EMPTY)
    override var enum: List<Any>? = null,
    @get:JsonInclude(NON_DEFAULT)
    override var readOnly: Boolean = false,
    @get:JsonInclude(NON_DEFAULT)
    override var writeOnly: Boolean = false,
    @get:JsonInclude(NON_NULL)
    override var format: String? = null,
    @get:JsonInclude(NON_NULL)
    override var minimum: Int? = null,
    @get:JsonInclude(NON_NULL)
    override var exclusiveMinimum: Int? = null,
    @get:JsonInclude(NON_NULL)
    override var maximum: Int? = null,
    @get:JsonInclude(NON_NULL)
    override var exclusiveMaximum: Int? = null,
    @get:JsonInclude(NON_NULL)
    override var multipleOf: Int? = null
) : BaseNumberSchema<Int>