package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

/**
 * An Interaction Affordance that exposes state of the Thing.
 * This state can then be retrieved (read) and/or updated (write).
 * Things can also choose to make Properties observable by pushing the new state after a change.
 */
interface PropertyAffordance<T> : InteractionAffordance, DataSchema<T> {

    /**
     * A hint that indicates whether Servients hosting the Thing and Intermediaries should provide a Protocol Binding that supports the observeproperty and unobserveproperty operations for this Property.
     *
     * Optional.
     */
    @get:JsonInclude(NON_NULL)
    var observable: Boolean
}