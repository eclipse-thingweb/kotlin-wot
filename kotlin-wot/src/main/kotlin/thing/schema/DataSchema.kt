package ai.ancf.lmos.wot.thing.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Metadata that describes the data format used. It can be used for validation.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
interface DataSchema<T> {

    /**
     * A keyword to label the object with semantic tags (or types).
     */
    val contextType: String? // Optional: JSON-LD keyword

    /**
     * A human-readable title based on a default language.
     */
    val title: String? // Optional: Human-readable title

    /**
     * Multi-language human-readable titles.
     */
    val titles: Map<String, String>? // Optional: Map of MultiLanguage

    /**
     * Additional human-readable information based on a default language.
     */
    val description: String? // Optional: Additional information

    /**
     * Multi-language descriptions.
     */
    val descriptions: Map<String, String>? // Optional: Map of MultiLanguage

    /**
     * Constant value.
     */
    val const: Any? // Optional: Constant value of any type

    /**
     * Default value.
     */
    val default: Any? // Optional: Default value of any type

    /**
     * Unit information.
     */
    val unit: String? // Optional: Unit information as a string

    /**
     * Validation against one of the specified schemas.
     */
    val oneOf: List<DataSchema<Any>>? // Optional: Array of DataSchema

    /**
     * Restricted set of values.
     */
    val enum: List<Any>? // Optional: Array of any type

    /**
     * Hint for read-only property.
     */
    val readOnly: Boolean? // Optional: Hint for read-only property, with default

    /**
     * Hint for write-only property.
     */
    val writeOnly: Boolean? // Optional: Hint for write-only property, with default

    /**
     * Format validation.
     */
    val format: String? // Optional: Format validation string

    /**
     * Assignment of JSON-based data types.
     */
    val type: String? // Optional: JSON-based data types (one of object, array, string, number, integer, boolean, or null)

}
