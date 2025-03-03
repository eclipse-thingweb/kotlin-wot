/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.binding.http.routes

import org.eclipse.thingweb.content.Content
import org.eclipse.thingweb.content.ContentManager
import org.eclipse.thingweb.thing.ExposedThing
import io.ktor.http.cio.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*

/**
 * Endpoint for listing all Things from the [io.github.sanecity.wot.Servient].
 */
class ThingsRoute(private val things: Map<String, ExposedThing>) : AbstractRoute() {
    @Throws(Exception::class)
    fun handle(request: RoutingRequest): Content {
        val requestContentType: String = getOrDefaultRequestContentType(request).toString()
        return ContentManager.valueToContent(things, requestContentType)
    }
}
