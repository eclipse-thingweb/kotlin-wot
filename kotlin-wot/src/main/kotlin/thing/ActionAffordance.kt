package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.schema.DataSchema


/**
 * Interface representing the details of an Action in a Web of Things context.
 */
interface ActionAffordance<I, O> : InteractionAffordance {

    /**
     * Used to define the input data schema of the Action.
     *
     * @return an optional data schema for input.
     */
    val input: DataSchema<I>? // Optional: DataSchema

    /**
     * Used to define the output data schema of the Action.
     *
     * @return an optional data schema for output.
     */
    val output: DataSchema<O>? // Optional: DataSchema

    /**
     * Signals if the Action is safe (true) or not.
     *
     * Used to indicate if no internal state (cf. resource state) is changed when invoking an Action.
     * In that case, responses can be cached, for example.
     *
     * @return true if the Action is safe; false otherwise.
     */
    val safe: Boolean // Default: true

    /**
     * Indicates whether the Action is idempotent (true) or not.
     *
     * Informs whether the Action can be called repeatedly with the same result, based on the same input.
     *
     * @return true if the Action is idempotent; false otherwise.
     */
    val idempotent: Boolean // Default: true

    /**
     * Indicates whether the action is synchronous (true) or not.
     *
     * A synchronous action means that the response of the action contains all the information about the result
     * of the action, and no further querying about the status of the action is needed.
     * Lack of this keyword means that no claim on the synchronicity of the action can be made.
     *
     * @return true if the Action is synchronous; false otherwise.
     */
    val synchronous: Boolean? // Optional: boolean
}