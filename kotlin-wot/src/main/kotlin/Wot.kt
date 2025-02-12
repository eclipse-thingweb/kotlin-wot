package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.security.NoSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.ThingDescription
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.schema.WoTConsumedThing
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import ai.ancf.lmos.wot.thing.schema.WoTThingDescription
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
    suspend fun discover(filter: ThingFilter): Flow<WoTExposedThing>

    /**
     * Starts the discovery process that will provide all available Things.
     *
     * @return
     */

    suspend fun discover(): Flow<WoTExposedThing>

    /**
     * Accepts a `thing` argument of type [ThingDescription] and returns an [ ] object.<br></br> The result can be used to start exposing interfaces for thing
     * interaction. Returns a failed future if thing with same id is already exposed.
     *
     * @param thingDescription
     * @return
     */
    fun produce(thingDescription: WoTThingDescription): WoTExposedThing

    fun produce(configure: ThingDescription.() -> Unit): WoTExposedThing

    /**
     * Accepts a `thing` argument of type [ThingDescription] and returns a [ConsumedThing] object.<br></br>
     *
     * The result can be used to interact with a thing.
     *
     * @param thingDescription
     * @return
     */
    fun consume(thingDescription: WoTThingDescription): WoTConsumedThing

    /**
     * Accepts an [java.net.URL] (e.g. "file:..." or "http://...") to a resource that serves a
     * thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    suspend fun requestThingDescription(url: URI, securityScheme: SecurityScheme = NoSecurityScheme()): WoTThingDescription

    /**
     * Accepts an [String] containing an url (e.g. "file:..." or "http://...") to a resource
     * that serves a thing description and returns the corresponding Thing object.
     *
     * @param url
     * @return
     */
    suspend fun requestThingDescription(url: String, securityScheme : SecurityScheme = NoSecurityScheme()): WoTThingDescription

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