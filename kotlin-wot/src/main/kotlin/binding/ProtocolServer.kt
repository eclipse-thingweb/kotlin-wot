package ai.anfc.lmos.wot.binding

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThing

/**
 * A ProtocolServer defines how to expose Thing for interaction via a specific protocol (e.g. HTTP,
 * MQTT, etc.).
 */
interface ProtocolServer {

    /**
     * Starts the server (e.g. HTTP server) and makes it ready for requests to the exposed things.
     *
     * @param servient
     * @return
     */
    suspend fun start(servient: Servient)

    /**
     * Stops the server (e.g. HTTP server) and ends the exposure of the Things
     *
     * @return
     */
    suspend fun stop()

    /**
     * Exposes `thing` and allows interaction with it.
     *
     * @param thing
     * @return
     */
    fun expose(thing: ExposedThing)

    /**
     * Stops the exposure of `thing` and allows no further interaction with the thing.
     *
     * @param thing
     * @return
     */
    suspend fun destroy(thing: ExposedThing)

}
