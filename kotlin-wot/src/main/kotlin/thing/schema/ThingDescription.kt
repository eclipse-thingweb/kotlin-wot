package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.Context
import ai.ancf.lmos.wot.thing.Link
import ai.ancf.lmos.wot.thing.VersionInfo
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Interface representing a Thing Description (TD) in a Web of Things context.
 */
interface ThingDescription : BaseSchema {

    /**
     * JSON-LD keyword to define short-hand names called terms that are used throughout a TD document.
     *
     * @return a URI or an array of URIs representing the context.
     */
    @get:JsonProperty("@context")
    var objectContext: Context? // Optional: anyURI or Array

    /**
     * Identifier of the Thing in form of a URI RFC3986.
     *
     * @return an optional URI identifier.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var id: String // Optional: anyURI

    /**
     * Provides version information.
     *
     * @return optional version information.
     */
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    var version: VersionInfo? // Optional: VersionInfo

    /**
     * Provides information when the TD instance was created.
     *
     * @return the creation date and time.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var created: String? // Optional: dateTime

    /**
     * Provides information when the TD instance was last modified.
     *
     * @return the last modified date and time.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var modified: String? // Optional: dateTime

    /**
     * Provides information about the TD maintainer as URI scheme (e.g., mailto, tel, https).
     *
     * @return an optional support URI.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var support: String? // Optional: anyURI

    /**
     * Define the base URI that is used for all relative URI references throughout a TD document.
     *
     * @return an optional base URI.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var base: String? // Optional: anyURI

    /**
     * All Property-based Interaction Affordances of the Thing.
     *
     * @return a map of property affordances.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var properties: MutableMap<String, ThingProperty<*>> // Optional: Map of PropertyAffordance

    /**
     * All Action-based Interaction Affordances of the Thing.
     *
     * @return a map of action affordances.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var actions: MutableMap<String, ThingAction<Any, Any>> // Optional: Map of ActionAffordance

    /**
     * All Event-based Interaction Affordances of the Thing.
     *
     * @return a map of event affordances.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var events: MutableMap<String, ThingEvent<Any, Any, Any>> // Optional: Map of EventAffordance

    /**
     * Provides Web links to arbitrary resources that relate to the specified Thing Description.
     *
     * @return an array of links.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var links: List<Link>? // Optional: Array of Link

    /**
     * Set of form hypermedia controls that describe how an operation can be performed.
     *
     * @return an array of forms.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var forms: List<Form> // Optional: Array of Form

    /**
     * Set of security definition names, chosen from those defined in securityDefinitions.
     *
     * @return a string or an array of strings representing security definitions, mandatory.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var security: List<String> // Mandatory: string or Array of string

    /**
     * Set of named security configurations (definitions only).
     *
     * @return a map of security schemes, mandatory.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var securityDefinitions: MutableMap<String, SecurityScheme> // Mandatory: Map of SecurityScheme

    /**
     * Indicates the WoT Profile mechanisms followed by this Thing Description and the corresponding Thing implementation.
     *
     * @return an optional profile URI or an array of URIs.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var profile: List<String>? // Optional: anyURI or Array of anyURI

    /**
     * Set of named data schemas to be used in a schema name-value pair.
     *
     * @return a map of data schemas, optional.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var schemaDefinitions: MutableMap<String, DataSchema<Any>>? // Optional: Map of DataSchema

    /**
     * Define URI template variables according to RFC6570 as collection based on DataSchema declarations.
     *
     * @return a map of URI variables.
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var uriVariables: MutableMap<String, DataSchema<Any>>? // Optional: Map of DataSchema
}