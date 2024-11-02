package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Data class representing a schema for string varues, with optional constraints such as minLength, maxLength, etc.
 *
 * @property minLength Specifies the minimum length of a string. Only applicable for associated string types.
 * @property maxLength Specifies the maximum length of a string. Only applicable for associated string types.
 * @property pattern Provides a regular expression to express constraints on the string varue. Follows the ECMA-262 dialect.
 * @property contentEncoding Specifies the encoding used to store the contents, as per RFC2045 and RFC4648.
 * @property contentMediaType Specifies the MIME type of the string contents, as described in RFC2046.
 */
@JsonTypeName("string")
data class StringSchema(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var objectType: Type? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var type: String? = "string",
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var const: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var default: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var unit: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var oneOf: List<DataSchema<Any>>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var enum: List<Any>? = null,
    @JsonInclude(NON_DEFAULT)
    override var readOnly: Boolean = false,
    @JsonInclude(NON_DEFAULT)
    override var writeOnly: Boolean = false,
    @JsonInclude(NON_NULL)
    override var format: String? = null,
    @JsonInclude(NON_NULL)
    var minLength: Int? = null,
    @JsonInclude(NON_NULL)
    var maxLength: Int? = null,
    @JsonInclude(NON_NULL)
    var pattern: String? = null,
    @JsonInclude(NON_NULL)
    var contentEncoding: String? = null,
    @JsonInclude(NON_NULL)
    var contentMediaType: String? = null
) : DataSchema<String> {


    override fun toString(): String {
        return "StringSchema{}"
    }

    companion object {
        const val TYPE = "string"
        var CLASS_TYPE: Class<String> = String::class.java
    }
}
