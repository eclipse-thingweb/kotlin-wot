package ai.ancf.lmos.wot.binding.websocket


import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.websocket.*
import kotlinx.coroutines.CompletableDeferred
import org.slf4j.LoggerFactory

class WebSocketProtocolClient(
    private val httpClientConfig: HttpClientConfig? = null,
    private val client: HttpClient = HttpClient(CIO) {
        install(WebSockets){
            contentConverter = JacksonWebsocketContentConverter()
        }
    }
) : ProtocolClient {
    companion object {
        private val log = LoggerFactory.getLogger(WebSocketProtocolClient::class.java)
    }

    private var session: DefaultClientWebSocketSession? = null

    override suspend fun start() {
        log.info("Starting WebSocketProtocolClient")
        client.webSocket(
            method = HttpMethod.Get,
            host = httpClientConfig?.address ?: "localhost",
            port = httpClientConfig?.port ?: 80,
            path = "/ws"
        ) {
            session = this
        }
    }

    override suspend fun stop() {
        log.info("Stopping WebSocketProtocolClient")
        session?.close()
        session = null
    }

    override suspend fun readResource(form: Form): Content {
        return resolveRequestToContent(form)
    }

    private suspend fun resolveRequestToContent(form: Form, content: Content? = null): Content {
        val response = CompletableDeferred<Content>()

        try {
            session?.let {
                it.sendSerialized(ReadPropertyMessage("test", property = "test"))

                val readingMessage = it.receiveDeserialized<PropertyReadingMessage>()

                val responseContent = Content(
                    body = readingMessage.data.binaryValue()
                )
                response.complete(responseContent)
            }
        } catch (e: Exception) {
            response.completeExceptionally(ProtocolClientException("Error during http request: ${e.message}", e))
        }
        return response.await()
    }
}