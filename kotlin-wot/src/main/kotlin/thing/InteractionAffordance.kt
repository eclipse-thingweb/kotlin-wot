package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.form.Form

/**
 * An interface representing an interaction affordance for a thing.
 * This interface defines properties and methods that provide metadata about the interaction affordance.
 */
interface InteractionAffordance {

    /**
     * JSON-LD keyword to label the object with semantic tags (or types).
     *
     * Optional. Can be a string or an array of strings.
     */
    var objectType: String?

    /**
     * Provides a human-readable title (e.g., display a text for UI representation)
     * based on a default language.
     *
     * Optional.
     */
    var title: String?

    /**
     * Provides multi-language human-readable titles (e.g., display a text for UI
     * representation in different languages). Also see MultiLanguage.
     *
     * Optional.
     */
    var titles: MutableMap<String, String>?

    /**
     * Provides additional (human-readable) information based on a default
     * language.
     *
     * Optional.
     */
    var description: String?

    /**
     * Can be used to support (human-readable) information in different languages.
     * Also see MultiLanguage.
     *
     * Optional.
     */
    var descriptions: MutableMap<String, String>?

    /**
     * Set of form hypermedia controls that describe how an operation can be performed.
     * Forms are serializations of Protocol Bindings. The array cannot be empty.
     *
     * Mandatory.
     */
    var forms: MutableList<Form>?

    /**
     * Define URI template variables according to [RFC6570] as a collection based
     * on DataSchema declarations. The individual variable DataSchemas cannot be an
     * ObjectSchema or an ArraySchema since each variable needs to be serialized to a
     * string inside the href upon the execution of the operation. If the same variable
     * is both declared in Thing level uriVariables and in Interaction Affordance level,
     * the Interaction Affordance level variable takes precedence.
     *
     * Optional.
     */
    var uriVariables: MutableMap<String, Map<String, Any>>?
}