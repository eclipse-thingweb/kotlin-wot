package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.schema.DataSchema

/**
 * Interface representing the details of an Event in a Web of Things context.
 */
interface EventAffordance<T, S, C> : InteractionAffordance {

    /**
     * Defines data that needs to be passed upon subscription, e.g., filters or message format for setting up Webhooks.
     *
     * @return an optional data schema for subscription.
     */
    var subscription: DataSchema<S>? // Optional: DataSchema

    /**
     * Defines the data schema of the Event instance messages pushed by the Thing.
     *
     * @return an optional data schema for event messages.
     */
    var data: DataSchema<T>? // Optional: DataSchema

    /**
     * Defines any data that needs to be passed to cancel a subscription, e.g., a specific message to remove a Webhook.
     *
     * @return an optional data schema for cancellation.
     */
    var cancellation: DataSchema<C>? // Optional: DataSchema
}