package ai.ancf.lmos.wot


import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.action.ExposedThingAction
import ai.ancf.lmos.wot.thing.event.ExposedThingEvent
import ai.ancf.lmos.wot.thing.filter.DiscoveryMethod
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import ai.ancf.lmos.wot.thing.schema.ThingDescription
import kotlinx.coroutines.flow.Flow
import java.net.URI
import java.net.URISyntaxException

/**
 * Standard implementation of [Wot].
 */
class DefaultWot(private val servient: Servient) : Wot {

    override fun toString(): String {
        return "DefaultWot{" +
                "servient=" + servient +
                '}'
    }
    @Throws(WotException::class)
    override suspend fun discover(filter: ThingFilter): Flow<ExposedThing> {
        return servient.discover(filter)
    }

    @Throws(WotException::class)
    override suspend fun discover(): Flow<ExposedThing> {
        return discover(ThingFilter(method = DiscoveryMethod.ANY))
    }

    override fun produce(thing: Thing): ExposedThing {
        val exposedThing = ExposedThing.from(thing)
        return if (servient.addThing(exposedThing)) {
            exposedThing
        } else {
            throw WotException("Thing already exists: " + thing.id)
        }
    }

    override fun produce(configure: Thing.() -> Unit): ExposedThing {
        val thing = Thing().apply(configure)
        return produce(thing)
    }

    override suspend fun fetch(url: URI): ExposedThing {
        return servient.fetch(url)
    }

    @Throws(URISyntaxException::class)
    override suspend fun fetch(url: String): ExposedThing {
        return servient.fetch(url)
    }

    suspend fun destroy() {
        return servient.shutdown()
    }
}
