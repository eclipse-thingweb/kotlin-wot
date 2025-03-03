/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.http.routes

import ai.ancf.lmos.wot.binding.http.routes.AbstractRoute
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.content.ContentManager.isSupportedMediaType
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * Abstract route for exposing Things. Inherited from all other routes.
 */
abstract class AbstractRoute {

    fun getOrDefaultRequestContentType(request: RoutingRequest): ContentType {
        val contentType = request.contentType()
        // Check if the content type is of type `Any` and return the default
        return if (contentType == ContentType.Any) {
            ContentType.Application.Json
        } else {
            contentType
        }
    }

    fun unsupportedMediaTypeResponse(response: RoutingResponse, requestContentType: String?): String? {
        return if (!isSupportedMediaType(requestContentType)) {
            response.status(HttpStatusCode.UnsupportedMediaType)
            "Unsupported Media Type (supported: " + java.lang.String.join(
                ", ",
                ContentManager.offeredMediaTypes
            ) + ")"
        } else {
            null
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(AbstractRoute::class.java)
    }
}
