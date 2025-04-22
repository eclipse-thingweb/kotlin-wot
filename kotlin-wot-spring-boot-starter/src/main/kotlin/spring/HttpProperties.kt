/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

open class ServerProperties(
    var enabled: Boolean = true,
    var host: String = "0.0.0.0",
    var port: Int = 8080,
    var baseUrls: List<String>
)


@ConfigurationProperties(prefix = "wot.servient.http.server", ignoreUnknownFields = true)
@Validated
class HttpServerProperties(
    enabled: Boolean = true,
    host: String = "0.0.0.0",
    port: Int = 8080,
    baseUrls: List<String> = listOf("http://localhost:$port")
) : ServerProperties(enabled, host, port, baseUrls)

@ConfigurationProperties(prefix = "wot.servient.websocket.server", ignoreUnknownFields = true)
@Validated
class WebsocketProperties(
    enabled: Boolean = true,
    host: String = "0.0.0.0",
    port: Int = 8080,
    baseUrls: List<String> = listOf("http://localhost:$port")
) : ServerProperties(enabled, host, port, baseUrls)