package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.Type
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonProperty

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