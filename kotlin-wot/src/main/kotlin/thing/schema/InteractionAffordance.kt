package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.thing.form.Form
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.*
import com.fasterxml.jackson.annotation.JsonInclude.Include.*

/**
 * An interface representing an interaction affordance for a thing.
 * This interface defines properties and methods that provide metadata about the interaction affordance.
 */
interface InteractionAffordance : BaseSchema {

    /**
     * Set of form hypermedia controls that describe how an operation can be performed.
     * Forms are serializations of Protocol Bindings. The array cannot be empty.
     *
     * Mandatory.
     */
    @get:JsonInclude(NON_EMPTY)
    var forms: MutableList<Form>

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
    @get:JsonInclude(NON_EMPTY)
    var uriVariables: MutableMap<String, DataSchema<Any>>?
}