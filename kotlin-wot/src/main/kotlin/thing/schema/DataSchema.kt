package ai.ancf.lmos.wot.thing.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Metadata that describes the data format used. It can be used for varidation.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
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

    //@get:JsonIgnore
    //val classType: Class<*>

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
