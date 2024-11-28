package websocket

import ai.anfc.lmos.wot.binding.ProtocolClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*

class WebSocketProtocolClient(
    private val httpClientConfig: HttpClientConfig? = null,
    private val client: HttpClient = HttpClient(CIO)
) : ProtocolClient {
    override suspend fun start() {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

}