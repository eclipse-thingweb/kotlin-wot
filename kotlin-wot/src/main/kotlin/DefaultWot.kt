package ai.ancf.lmos.wot


import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.ThingDescription
import ai.ancf.lmos.wot.thing.filter.DiscoveryMethod
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
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
    override suspend fun discover(filter: ThingFilter): Flow<WoTExposedThing> {
        return servient.discover(filter)
    }

    @Throws(WotException::class)
    override suspend fun discover(): Flow<WoTExposedThing> {
        return discover(ThingFilter(method = DiscoveryMethod.ANY))
    }

    override fun produce(thingDescription: ThingDescription): ExposedThing {
        val exposedThing = ExposedThing(servient, thingDescription)
        return if (servient.addThing(exposedThing)) {
            exposedThing
        } else {
            throw WotException("Thing already exists: " + thingDescription.id)
        }
    }

    override fun produce(configure: ThingDescription.() -> Unit): ExposedThing {
        val thingDescription = ThingDescription().apply(configure)
        return produce(thingDescription)
    }

    override fun consume(thingDescription: ThingDescription) = ConsumedThing(servient, thingDescription)

    override suspend fun requestThingDescription(url: URI): ThingDescription {
        return servient.fetch(url)
    }

    @Throws(URISyntaxException::class)
    override suspend fun requestThingDescription(url: String): ThingDescription {
        return servient.fetch(url)
    }

    suspend fun destroy() {
        return servient.shutdown()
    }
}
