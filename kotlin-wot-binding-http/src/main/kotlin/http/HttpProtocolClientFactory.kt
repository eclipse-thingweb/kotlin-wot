package ai.ancf.lmos.wot.binding.http

import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import http.HttpClientConfig

/**
 * Creates new [HttpProtocolClient] instances.
 */
open class HttpProtocolClientFactory(private val httpClientConfig: HttpClientConfig? = null) : ProtocolClientFactory {
    override fun toString(): String {
        return "HttpClient"
    }
    override val scheme: String
        get() = "http"
    override val client: HttpProtocolClient
        get() = HttpProtocolClient(httpClientConfig)

    override suspend fun init() {
       // TODO
    }

    override suspend fun destroy() {
        // TODO
    }
}
