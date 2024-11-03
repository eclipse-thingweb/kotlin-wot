package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.thing.schema.EventAffordance
import ai.ancf.lmos.wot.thing.event.ThingEvent
import java.util.*

/**
 * Used in combination with [ConsumedThing] and allows consuming of a [ThingEvent].
 */
class ConsumedThingEvent<T, S, C>(private val event: ThingEvent<T, S, C>) : EventAffordance<T, S, C> by event {


}
