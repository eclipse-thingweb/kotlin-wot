package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

interface CommonSchema {

    /**
     * A keyword to label the object with semantic tags (or types).
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    @get:JsonProperty("@type")
    var objectType: String? // Optional: JSON-LD keyword

    /**
     * A human-readable title based on a default language.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var title: String? // Optional: Human-readable title

    /**
     * Multi-language human-readable titles.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var titles: MutableMap<String, String>? // Optional: Map of MultiLanguage

    /**
     * Additional human-readable information based on a default language.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var description: String? // Optional: Additional information

    /**
     * Multi-language descriptions.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var descriptions: MutableMap<String, String>? // Optional: Map of MultiLanguage
}