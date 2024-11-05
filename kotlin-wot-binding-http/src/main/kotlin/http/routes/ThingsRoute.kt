package ai.ancf.lmos.wot.binding.http.routes

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThing
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
