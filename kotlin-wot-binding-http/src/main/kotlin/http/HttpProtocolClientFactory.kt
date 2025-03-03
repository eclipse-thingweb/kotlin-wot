/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.http

import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientFactory

/**
 * Creates new [HttpProtocolClient] instances.
 */
open class HttpProtocolClientFactory() : ProtocolClientFactory {
    override fun toString(): String {
        return "HttpClient"
    }
    override val scheme: String
        get() = "http"

    override suspend fun init() {
       // TODO
    }

    override suspend fun destroy() {
        // TODO
    }

    override fun createClient(): ProtocolClient = HttpProtocolClient()
}
