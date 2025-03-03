/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb

import org.eclipse.thingweb.security.NoSecurityScheme
import org.eclipse.thingweb.security.SecurityScheme
import org.eclipse.thingweb.thing.ConsumedThing
import org.eclipse.thingweb.thing.ThingDescription
import org.eclipse.thingweb.thing.filter.ThingFilter
import org.eclipse.thingweb.thing.schema.WoTConsumedThing
import org.eclipse.thingweb.thing.schema.WoTExposedThing
import org.eclipse.thingweb.thing.schema.WoTThingDescription
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
    fun discover(filter: ThingFilter): Flow<WoTThingDescription>

    /**
     * Starts the discovery process that will provide all available Things.
     *
     * @return
     */
    fun discover(): Flow<WoTThingDescription>

    /**
     * Starts the discovery process that will provide Things that match the `filter`
     * argument from a given Thing Directory.
     *
     * @param filter
     * @return
     */
    suspend fun exploreDirectory(directoryUrl: String, securityScheme: SecurityScheme = NoSecurityScheme()): Set<WoTThingDescription>

    /**
     * Accepts a `thing` argument of type [ThingDescription] and returns an [WoTExposedThing].
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