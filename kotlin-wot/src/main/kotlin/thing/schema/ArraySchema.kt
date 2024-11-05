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
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "array",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: List<*>? = null,
    @JsonInclude(NON_EMPTY)
    override var default: List<*>? = null,
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
    open var items: List<DataSchema<T>>,
    @JsonInclude(NON_NULL)
    var minItems: Int? = null,          // Minimum number of items
    @JsonInclude(NON_NULL)
    var maxItems: Int? = null           // Maximum number of items
) : DataSchema<List<*>>{
    override val classType: Class<List<*>>
        get() = List::class.java

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArraySchema<*>

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
        if (items != other.items) return false
        if (minItems != other.minItems) return false
        if (maxItems != other.maxItems) return false

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
        result = 31 * result + (items.hashCode())
        result = 31 * result + (minItems ?: 0)
        result = 31 * result + (maxItems ?: 0)
        return result
    }
}