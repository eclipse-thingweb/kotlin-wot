package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Describes data of type [number](https://www.w3.org/TR/wot-thing-description/#numberschema).
 */
@JsonTypeName("null")
open class NumberSchema(
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    @JsonInclude(NON_EMPTY)
    override var type: String? = "null",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: Number? = null,
    @JsonInclude(NON_EMPTY)
    override var default: Number? = null,
    @JsonInclude(NON_EMPTY)
    override var unit: String? = null,
    @JsonInclude(NON_EMPTY)
    override var oneOf: List<DataSchema<Any>>? = null,
    @JsonInclude(NON_EMPTY)
    override var enum: List<Any>? = null,
    @JsonInclude(NON_DEFAULT)
    override var readOnly: Boolean = false,
    @JsonInclude(NON_DEFAULT)
    override var writeOnly: Boolean = false,
    @JsonInclude(NON_NULL)
    override var format: String? = null,
    @JsonInclude(NON_NULL)
    override var minimum: Int? = null,
    @JsonInclude(NON_NULL)
    override var exclusiveMinimum: Int? = null,
    @JsonInclude(NON_NULL)
    override var maximum: Int? = null,
    @JsonInclude(NON_NULL)
    override var exclusiveMaximum: Int? = null,
    @JsonInclude(NON_NULL)
    override var multipleOf: Int? = null
) : BaseNumberSchema<Number>{

}