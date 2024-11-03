package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Describes data of type [object](https://www.w3.org/TR/wot-thing-description/#objectschema).
 */
@JsonTypeName("object")
open class ObjectSchema(
    @JsonInclude(NON_EMPTY) val properties: Map<String?, DataSchema<Any>?> = HashMap(),
    @JsonInclude(NON_EMPTY) val required: List<String?> = ArrayList(),
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    @JsonInclude(NON_EMPTY)
    override var type: String? = "object",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: Map<Any, Any>? = null,
    @JsonInclude(NON_EMPTY)
    override var default: Map<Any, Any>? = null,
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
    override var format: String? = null
) : DataSchema<Map<Any, Any>>{

}