package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.thing.ConsumedThingImpl
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.schema.ConsumedThing
import ai.ancf.lmos.wot.thing.schema.ExposedThing
import kotlinx.coroutines.flow.Flow
import java.net.URI

/**
 * Provides methods for discovering, consuming, exposing and fetching things.
 * https://w3c.github.io/wot-scripting-api/#the-wot-api-object
 */
interface Wot {

    /**
     * Starts the discovery process that will provide Things that match the `filter`
     * argument.
     *
     * @param filter
     * @return
     */
    suspend fun discover(filter: ThingFilter): Flow<ExposedThing>

    /**
     * Starts the discovery process that will provide all available Things.
     *
     * @return
     */

    suspend fun discover(): Flow<ExposedThing>

    /**
     * Accepts a `thing` argument of type [Thing] and returns an [ ] object.<br></br> The result can be used to start exposing interfaces for thing
     * interaction. Returns a failed future if thing with same id is already exposed.
     *
     * @param thing
     * @return
     */
    fun produce(thing: Thing): ExposedThing

    fun produce(configure: Thing.() -> Unit): ExposedThing

    /**
     * Accepts a `thing` argument of type [Thing] and returns a [ConsumedThingImpl] object.<br></br>
     *
     * The result can be used to interact with a thing.
     *
     * @param thing
     * @return
     */
    fun consume(thing: Thing): ConsumedThing

    /**
     * Accepts an [java.net.URL] (e.g. "file:..." or "http://...") to a resource that serves a
     * thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    suspend fun requestThingDescription(url: URI): Thing

    /**
     * Accepts an [String] containing an url (e.g. "file:..." or "http://...") to a resource
     * that serves a thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    suspend fun requestThingDescription(url: String): Thing

    companion object {
        // Factory method to create an instance of WoT with a given Servient
        fun create(servient: Servient): Wot {
            return DefaultWot(servient)
        }
    }

}


open class WotException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String, cause: Throwable): super(message, cause)

    constructor() : super()
}

@DslMarker
annotation class WoTDSL