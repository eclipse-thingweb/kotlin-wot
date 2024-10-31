package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.filter.ThingFilter
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
    suspend fun discover(filter: ThingFilter): Flow<Thing>

    /**
     * Starts the discovery process that will provide all available Things.
     *
     * @return
     */

    suspend fun discover(): Flow<Thing>

    /**
     * Accepts a `thing` argument of type [Thing] and returns an [ ] object.<br></br> The result can be used to start exposing interfaces for thing
     * interaction. Returns a failed future if thing with same id is already exposed.
     *
     * @param thing
     * @return
     * @throws WotException If thing with same id is already exposed
     */
    fun produce(thing: Thing): ExposedThing

    fun produce(configure: Thing.() -> Unit): ExposedThing

    /**
     * Accepts an [java.net.URL] (e.g. "file:..." or "http://...") to a resource that serves a
     * thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    suspend fun fetch(url: URI): Thing

    /**
     * Accepts an [String] containing an url (e.g. "file:..." or "http://...") to a resource
     * that serves a thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    suspend fun fetch(url: String): Thing

}


open class WotException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String, cause: Exception): super(message, cause)

    constructor() : super()
}