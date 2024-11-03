package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Describes data of type [Array](https://www.w3.org/TR/wot-thing-description/#arrayschema).
 */
@JsonTypeName("array")
open class ArraySchema<T>(
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    @JsonInclude(NON_EMPTY)
    override var type: String? = "array",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: List<Any>? = null,
    @JsonInclude(NON_EMPTY)
    override var default: List<Any>? = null,
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
    @JsonInclude(NON_EMPTY)
    var items: List<DataSchema<T>>? = null,
    @JsonInclude(NON_NULL)
    var minItems: Int? = null,          // Minimum number of items
    @JsonInclude(NON_NULL)
    var maxItems: Int? = null           // Maximum number of items
) : DataSchema<List<Any>>{

}
