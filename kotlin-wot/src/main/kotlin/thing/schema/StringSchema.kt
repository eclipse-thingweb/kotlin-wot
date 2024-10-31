package ai.ancf.lmos.wot.thing.schema

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

/**
 * Data class representing a schema for string values, with optional constraints such as minLength, maxLength, etc.
 *
 * @property minLength Specifies the minimum length of a string. Only applicable for associated string types.
 * @property maxLength Specifies the maximum length of a string. Only applicable for associated string types.
 * @property pattern Provides a regular expression to express constraints on the string value. Follows the ECMA-262 dialect.
 * @property contentEncoding Specifies the encoding used to store the contents, as per RFC2045 and RFC4648.
 * @property contentMediaType Specifies the MIME type of the string contents, as described in RFC2046.
 */
data class StringSchema(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val contextType: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val title: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val titles: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val description: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val descriptions: Map<String, String>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val const: Any? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val default: Any? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val unit: String? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val oneOf: List<DataSchema<Any>>? = null,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    override val enum: List<Any>? = null,
    @JsonInclude(NON_DEFAULT)
    override val readOnly: Boolean? = false,
    @JsonInclude(NON_DEFAULT)
    override val writeOnly: Boolean? = false,
    @JsonInclude(NON_NULL)
    override val format: String? = null,
    @JsonInclude(NON_NULL)
    val minLength: Int? = null,
    @JsonInclude(NON_NULL)
    val maxLength: Int? = null,
    @JsonInclude(NON_NULL)
    val pattern: String? = null,
    @JsonInclude(NON_NULL)
    val contentEncoding: String? = null,
    @JsonInclude(NON_NULL)
    val contentMediaType: String? = null
) : AbstractDataSchema<String>() {

    override val type: String
        get() = TYPE

    @get:JsonIgnore
    override val classType: Class<String>
        get() = String::class.java

    override fun toString(): String {
        return "StringSchema{}"
    }

    companion object {
        const val TYPE = "string"
        val CLASS_TYPE: Class<String> = String::class.java
    }
}
