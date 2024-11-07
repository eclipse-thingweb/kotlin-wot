package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY

sealed interface BaseSchema {

    /**
     * A keyword to label the object with semantic tags (or types).
     */
    @get:JsonInclude(NON_EMPTY)
    @get:JsonProperty("@type")
    var objectType: Type? // Optional: JSON-LD keyword

    /**
     * A human-readable title based on a default language.
     */
    @get:JsonInclude(NON_EMPTY)
    var title: String? // Optional: Human-readable title

    /**
     * Multi-language human-readable titles.
     */
    @get:JsonInclude(NON_EMPTY)
    var titles: MutableMap<String, String>? // Optional: Map of MultiLanguage

    /**
     * Additional human-readable information based on a default language.
     */
    @get:JsonInclude(NON_EMPTY)
    var description: String? // Optional: Additional information

    /**
     * Multi-language descriptions.
     */
    @get:JsonInclude(NON_EMPTY)
    var descriptions: MutableMap<String, String>? // Optional: Map of MultiLanguage
}

/**
 * Metadata that describes the data format used. It can be used for varidation.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = StringSchema::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = StringSchema::class, name = "string") ,
    JsonSubTypes.Type(value = IntegerSchema::class, name = "integer"),
    JsonSubTypes.Type(value = NumberSchema::class, name = "number"),
    JsonSubTypes.Type(value = BooleanSchema::class, name = "boolean"),
    JsonSubTypes.Type(value = ArraySchema::class, name = "array"),
    JsonSubTypes.Type(value = ObjectSchema::class, name = "object"),
    JsonSubTypes.Type(value = NullSchema::class, name = "null")
)
sealed interface DataSchema<T> : BaseSchema {

    /**
     * Constant value.
     */
    @get:JsonInclude(NON_EMPTY)
    var const: T? // Optional: Constant value of any type

    /**
     * Default value.
     */
    @get:JsonInclude(NON_EMPTY)
    var default: T? // Optional: Default value of any type

    /**
     * Unit information.
     */
    @get:JsonInclude(NON_EMPTY)
    var unit: String? // Optional: Unit information as a string

    /**
     * varidation against one of the specified schemas.
     */
    @get:JsonInclude(NON_EMPTY)
    var oneOf: List<DataSchema<Any>>? // Optional: Array of DataSchema

    /**
     * Restricted set of values.
     */
    @get:JsonInclude(NON_EMPTY)
    var enum: List<Any>? // Optional: Array of any type

    /**
     * Hint for read-only property.
     */
    @get:JsonInclude(NON_DEFAULT)
    var readOnly: Boolean // Optional: Hint for read-only property, with default

    /**
     * Hint for write-only property.
     */
    @get:JsonInclude(NON_DEFAULT)
    var writeOnly: Boolean // Optional: Hint for write-only property, with default

    /**
     * Format varidation.
     */
    @get:JsonInclude(NON_EMPTY)
    var format: String? // Optional: Format validation string

    /**
     * Assignment of JSON-based data types.
     */
    //@get:JsonInclude(NON_EMPTY)
    //var type: String? // Optional: JSON-based data types (one of object, array, string, number, integer, boolean, or null)

    @get:JsonIgnore
    val classType: Class<T>

}

sealed interface BaseNumberSchema<T> : DataSchema<T>{

    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    var minimum: Int?
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    var exclusiveMinimum: Int?
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    var maximum: Int?
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    var exclusiveMaximum: Int?
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    var multipleOf: Int?
}

/**
 * Describes data of type [boolean](https://www.w3.org/TR/wot-thing-description/#booleanschema).
 */
@JsonTypeName("boolean")
open class BooleanSchema(
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "boolean",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: Boolean? = null,
    @JsonInclude(NON_EMPTY)
    override var default: Boolean? = null,
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null
) : DataSchema<Boolean>{

    override val classType: Class<Boolean>
        get() = Boolean::class.java

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BooleanSchema

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
        return result
    }
}

fun booleanSchema(initializer: BooleanSchema.() -> Unit): BooleanSchema {
    return BooleanSchema().apply(initializer)
}

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
    //@get:JsonInclude(NON_EMPTY)
    //override var type: String? = "integer",
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
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    override var minimum: Int? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    override var exclusiveMinimum: Int? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    override var maximum: Int? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    override var exclusiveMaximum: Int? = null,
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    override var multipleOf: Int? = null
) : BaseNumberSchema<Int>{

    override val classType: Class<Int>
        get() = Int::class.java

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntegerSchema

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
        result = 31 * result + (const ?: 0)
        result = 31 * result + (default ?: 0)
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

fun integerSchema(initializer: IntegerSchema.() -> Unit): IntegerSchema {
    return IntegerSchema().apply(initializer)
}


/**
 * Describes data of type [null](https://www.w3.org/TR/wot-thing-description/#nullschema).
 */
@JsonTypeName("null")
open class NullSchema(
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
    override var const: Any? = null,
    @JsonInclude(NON_EMPTY)
    override var default: Any? = null,
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null
) : DataSchema<Any>{

    override val classType: Class<Any>
        get() = Any::class.java

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NullSchema

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
        return result
    }
}


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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var minimum: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var exclusiveMinimum: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var maximum: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var exclusiveMaximum: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var multipleOf: Int? = null
) : BaseNumberSchema<Number>{

    override val classType: Class<Number>
        get() = Number::class.java

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

fun numberSchema(initializer: NumberSchema.() -> Unit): NumberSchema {
    return NumberSchema().apply(initializer)
}


/**
 * Describes data of type [object](https://www.w3.org/TR/wot-thing-description/#objectschema).
 */
@JsonTypeName("object")
open class ObjectSchema(
    @JsonInclude(NON_EMPTY) val properties: Map<String?, DataSchema<Any>?> = HashMap(),
    @JsonInclude(NON_EMPTY) val required: List<String?> = ArrayList(),
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "object",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: Map<*, *>? = null,
    @JsonInclude(NON_EMPTY)
    override var default: Map<*, *>? = null,
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null
) : DataSchema<Map<*, *>>{

    override val classType: Class<Map<*, *>>
        get() = Map::class.java

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectSchema

        if (properties != other.properties) return false
        if (required != other.required) return false
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

        return true
    }

    override fun hashCode(): Int {
        var result = properties.hashCode()
        result = 31 * result + required.hashCode()
        result = 31 * result + (objectType?.hashCode() ?: 0)
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
        return result
    }
}

fun objectSchema(initializer: ObjectSchema.() -> Unit): ObjectSchema {
    return ObjectSchema().apply(initializer)
}
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
open class StringSchema(
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "string",
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,
    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var description: String? = null,
    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,
    @JsonInclude(NON_EMPTY)
    override var const: String? = null,
    @JsonInclude(NON_EMPTY)
    override var default: String? = null,
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var minLength: Int? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var maxLength: Int? = null,
    @JsonInclude(NON_EMPTY)
    var pattern: String? = null,
    @JsonInclude(NON_EMPTY)
    var contentEncoding: String? = null,
    @JsonInclude(NON_EMPTY)
    var contentMediaType: String? = null
) : DataSchema<String> {

    override val classType: Class<String>
        get() = String::class.java

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringSchema

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
        if (minLength != other.minLength) return false
        if (maxLength != other.maxLength) return false
        if (pattern != other.pattern) return false
        if (contentEncoding != other.contentEncoding) return false
        if (contentMediaType != other.contentMediaType) return false

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
        result = 31 * result + (minLength ?: 0)
        result = 31 * result + (maxLength ?: 0)
        result = 31 * result + (pattern?.hashCode() ?: 0)
        result = 31 * result + (contentEncoding?.hashCode() ?: 0)
        result = 31 * result + (contentMediaType?.hashCode() ?: 0)
        return result
    }
}

fun stringSchema(initializer: StringSchema.() -> Unit): StringSchema {
    return StringSchema().apply(initializer)
}


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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override var format: String? = null,
    @JsonInclude(NON_EMPTY)
    open var items: List<DataSchema<T>>? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var minItems: Int? = null,          // Minimum number of items
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var maxItems: Int? = null           // Maximum number of items
) : DataSchema<List<*>> {
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





