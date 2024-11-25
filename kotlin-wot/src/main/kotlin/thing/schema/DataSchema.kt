package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.WoTDSL
import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.*

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
@WoTDSL
sealed interface DataSchema<T> : BaseSchema {

    fun validate(value: T): List<ValidationException>

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

    @get:JsonInclude(NON_NULL)
    var minimum: T?
    @get:JsonInclude(NON_NULL)
    var exclusiveMinimum: T?
    @get:JsonInclude(NON_NULL)
    var maximum: T?
    @get:JsonInclude(NON_NULL)
    var exclusiveMaximum: T?
    @get:JsonInclude(NON_NULL)
    var multipleOf: T?
}

/**
 * Describes data of type [boolean](https://www.w3.org/TR/wot-thing-description/#booleanschema).
 */
@JsonTypeName("boolean")
open class BooleanSchema(
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "boolean",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: Boolean? = null,
    override var default: Boolean? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null
) : DataSchema<Boolean>{

    // Validation function specific to BooleanSchema
    override fun validate(value: Boolean): List<ValidationException> {
        return Validators.validateConst(value, const)
    }

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
    override var objectType: Type? = null,
    //@get:JsonInclude(NON_EMPTY)
    //override var type: String? = "integer",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: Int? = null,
    override var default: Int? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null,
    override var minimum: Int? = null,
    override var exclusiveMinimum: Int? = null,
    override var maximum: Int? = null,
    override var exclusiveMaximum: Int? = null,
    override var multipleOf: Int? = null
) : BaseNumberSchema<Int>{

    override fun validate(value: Int): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        exceptions.addAll(Validators.validateBounds(value, minimum, maximum, exclusiveMinimum, exclusiveMaximum))
        exceptions.addAll(Validators.validateMultipleOf(value, multipleOf))
        exceptions.addAll(Validators.validateEnum(value, enum))
        return exceptions
    }

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
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "null",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: Any? = null,
    override var default: Any? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null
) : DataSchema<Any>{

    override fun validate(value: Any): List<ValidationException> {
        return emptyList()
    }

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
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "null",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: Number? = null,
    override var default: Number? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null,
    override var minimum: Number? = null,
    override var exclusiveMinimum: Number? = null,
    override var maximum: Number? = null,
    override var exclusiveMaximum: Number? = null,
    override var multipleOf: Number? = null
) : BaseNumberSchema<Number>{

    override fun validate(value: Number): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        exceptions.addAll(Validators.validateBounds(value, minimum, maximum, exclusiveMinimum, exclusiveMaximum))
        exceptions.addAll(Validators.validateMultipleOf(value, multipleOf))
        exceptions.addAll(Validators.validateEnum(value, enum))
        return exceptions
    }

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
        result = 31 * result + (minimum?.hashCode() ?: 0)
        result = 31 * result + (exclusiveMinimum?.hashCode() ?: 0)
        result = 31 * result + (maximum?.hashCode() ?: 0)
        result = 31 * result + (exclusiveMaximum?.hashCode() ?: 0)
        result = 31 * result + (multipleOf?.hashCode() ?: 0)
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
    @get:JsonInclude(NON_EMPTY) val properties: MutableMap<String, DataSchema<*>> = mutableMapOf(),
    @get:JsonInclude(NON_EMPTY) val required: MutableList<String> = mutableListOf(),
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "object",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: Map<*, *>? = null,
    override var default: Map<*, *>? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null
) : DataSchema<Map<*, *>>{
    override fun validate(value: Map<*, *>): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        properties.forEach { (key, schema) ->
            val propValue = value[key]
            val propExceptions = when (schema) {
                is StringSchema -> if (propValue is String) schema.validate(propValue) else listOf(ArrayItemsException("Item $propValue is not a String"))
                is IntegerSchema -> if (propValue is Int) schema.validate(propValue) else listOf(ArrayItemsException("Item $propValue is not an Integer"))
                is BooleanSchema -> if (propValue is Boolean) schema.validate(propValue) else listOf(ArrayItemsException("Item $propValue is not a Boolean"))
                is NumberSchema -> if (propValue is Number) schema.validate(propValue) else listOf(ArrayItemsException("Item $propValue is not a Number"))
                is ObjectSchema -> if (propValue is Map<*, *>) schema.validate(propValue) else listOf(ArrayItemsException("Item $propValue is not an Object"))
                is ArraySchema<*> -> if (propValue is List<*>) schema.validate(propValue) else listOf(ArrayItemsException("Item $propValue is not an Object"))
                else -> listOf(ArrayItemsException("Unknown item type"))
            }
            exceptions.addAll(propExceptions)
        }
        return exceptions
    }

    fun stringProperty(name: String, configure: StringSchema.() -> Unit) {
        this.properties[name] = StringSchema().apply(configure)
    }

    fun booleanProperty(name: String, configure: BooleanSchema.() -> Unit) {
        this.properties[name] = BooleanSchema().apply(configure)
    }

    fun integerProperty(name: String, configure: IntegerSchema.() -> Unit) {
        this.properties[name] = IntegerSchema().apply(configure)
    }

    fun numberProperty(name: String, configure: NumberSchema.() -> Unit) {
        this.properties[name] = NumberSchema().apply(configure)
    }

    fun objectProperty(name: String, configure: ObjectSchema.() -> Unit) {
        this.properties[name] = ObjectSchema().apply(configure)
    }

    fun <T> arrayProperty(name: String, configure: ArraySchema<T>.() -> Unit) {
        this.properties[name] = ArraySchema<T>().apply(configure)
    }

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

    override fun toString(): String {
        return "ObjectSchema(properties=$properties, required=$required, objectType=$objectType, title=$title, titles=$titles, description=$description, descriptions=$descriptions, const=$const, default=$default, unit=$unit, oneOf=$oneOf, enum=$enum, readOnly=$readOnly, writeOnly=$writeOnly, format=$format)"
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
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "string",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: String? = null,
    override var default: String? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null,
    @get:JsonInclude(NON_NULL)
    var minLength: Int? = null,
    @get:JsonInclude(NON_NULL)
    var maxLength: Int? = null,
    @get:JsonInclude(NON_EMPTY)
    var pattern: String? = null,
    @get:JsonInclude(NON_EMPTY)
    var contentEncoding: String? = null,
    @get:JsonInclude(NON_EMPTY)
    var contentMediaType: String? = null
) : DataSchema<String> {

    // Validation function specific to StringSchema
    override fun validate(value: String): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        exceptions.addAll(Validators.validateStringLength(value, minLength, maxLength))
        exceptions.addAll(Validators.validatePattern(value, pattern))
        exceptions.addAll(Validators.validateEnum(value, enum))
        return exceptions
    }

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
    override var objectType: Type? = null,
    //@JsonInclude(NON_EMPTY)
    //override var type: String? = "array",
    override var title: String? = null,
    override var titles: MutableMap<String, String>? = null,
    override var description: String? = null,
    override var descriptions: MutableMap<String, String>? = null,
    override var const: List<*>? = null,
    override var default: List<*>? = null,
    override var unit: String? = null,
    override var oneOf: List<DataSchema<Any>>? = null,
    override var enum: List<Any>? = null,
    override var readOnly: Boolean = false,
    override var writeOnly: Boolean = false,
    override var format: String? = null,
    @get:JsonInclude(NON_EMPTY)
    var items: DataSchema<T>? = null,
    @get:JsonInclude(NON_NULL)
    var minItems: Int? = null,          // Minimum number of items
    @get:JsonInclude(NON_NULL)
    var maxItems: Int? = null           // Maximum number of items
) : DataSchema<List<*>> {

    override fun validate(value: List<*>): List<ValidationException> {
        val exceptions = mutableListOf<ValidationException>()
        exceptions.addAll(Validators.validateArrayLength(value, minItems, maxItems))
        exceptions.addAll(Validators.validateArrayItems(value, items))
        return exceptions
    }

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





