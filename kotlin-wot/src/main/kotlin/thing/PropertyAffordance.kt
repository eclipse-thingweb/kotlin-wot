package ai.ancf.lmos.wot.thing

/**
 * An Interaction Affordance that exposes state of the Thing.
 * This state can then be retrieved (read) and/or updated (write).
 * Things can also choose to make Properties observable by pushing the new state after a change.
 */
interface PropertyAffordance : InteractionAffordance {

    /**
     * A hint that indicates whether Servients hosting the Thing and Intermediaries should provide a Protocol Binding that supports the observeproperty and unobserveproperty operations for this Property.
     *
     * Optional.
     */
    var observable: Boolean
}