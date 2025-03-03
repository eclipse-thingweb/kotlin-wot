/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.websocket

import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientFactory

/**
 * Creates new [WebSocketProtocolClient] instances.
 */
open class WebSocketProtocolClientFactory(private val httpClientConfig: HttpClientConfig? = null) : ProtocolClientFactory {
    override fun toString(): String {
        return "WebSocketProtocolClient"
    }
    override val scheme: String
        get() = "ws"

    override suspend fun init() {

    }

    override suspend fun destroy() {

    }

    override fun createClient(): ProtocolClient = WebSocketProtocolClient(httpClientConfig)

}
