package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.CommonSchema
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY

/**
 * Metadata that describes the data format used. It can be used for varidation.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
interface DataSchema<T> : CommonSchema {

    /**
     * Constant varue.
     */
    @get:JsonInclude(NON_EMPTY)
    var const: T? // Optional: Constant varue of any type

    /**
     * Default varue.
     */
    @get:JsonInclude(NON_EMPTY)
    var default: T? // Optional: Default varue of any type

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
     * Restricted set of varues.
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
    var format: String? // Optional: Format varidation string

    /**
     * Assignment of JSON-based data types.
     */
    @get:JsonInclude(NON_EMPTY)
    var type: String? // Optional: JSON-based data types (one of object, array, string, number, integer, boolean, or null)

}
