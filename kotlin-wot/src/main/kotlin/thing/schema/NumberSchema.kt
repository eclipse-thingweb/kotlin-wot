package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Describes data of type [number](https://www.w3.org/TR/wot-thing-description/#numberschema).
 */
@JsonTypeName("number")
open class NumberSchema(
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "null",
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NumberSchema

        if (objectType != other.objectType) return false
        if (title != other.title) return false
        if (titles != other.titles) return false
        if (description != other.description) return false
        if (descriptions != other.descriptions) return false
        if (const != other.const) return false
        if (default != other.default) return false
        if (unit != other.unit) return false
        if (oneOf != other.oneOf) return false
        if (enum != other.enum) return false
        if (readOnly != other.readOnly) return false
        if (writeOnly != other.writeOnly) return false
        if (format != other.format) return false
        if (minimum != other.minimum) return false
        if (exclusiveMinimum != other.exclusiveMinimum) return false
        if (maximum != other.maximum) return false
        if (exclusiveMaximum != other.exclusiveMaximum) return false
        if (multipleOf != other.multipleOf) return false

        return true
    }

    override fun hashCode(): Int {
        var result = objectType?.hashCode() ?: 0
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (titles?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (descriptions?.hashCode() ?: 0)
        result = 31 * result + (const?.hashCode() ?: 0)
        result = 31 * result + (default?.hashCode() ?: 0)
        result = 31 * result + (unit?.hashCode() ?: 0)
        result = 31 * result + (oneOf?.hashCode() ?: 0)
        result = 31 * result + (enum?.hashCode() ?: 0)
        result = 31 * result + readOnly.hashCode()
        result = 31 * result + writeOnly.hashCode()
        result = 31 * result + (format?.hashCode() ?: 0)
        result = 31 * result + (minimum ?: 0)
        result = 31 * result + (exclusiveMinimum ?: 0)
        result = 31 * result + (maximum ?: 0)
        result = 31 * result + (exclusiveMaximum ?: 0)
        result = 31 * result + (multipleOf ?: 0)
        return result
    }
}