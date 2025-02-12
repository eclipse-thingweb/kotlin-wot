package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
import ai.ancf.lmos.wot.integration.Chat
import ai.ancf.lmos.wot.thing.ConsumedThing


class ConversationalAgent private constructor(private val thing : ConsumedThing) {

    companion object {
        suspend fun create(wot: Wot, url: String): ConversationalAgent {
            return ConversationalAgent(wot.consume(wot.requestThingDescription(url)) as ConsumedThing)
        }

        suspend fun create(url: String): ConversationalAgent {
            val wot = Wot.create(Servient(clientFactories = listOf(HttpProtocolClientFactory(), HttpsProtocolClientFactory(), WebSocketProtocolClientFactory())))
            return create(wot, url)
        }
    }

    suspend fun chat(inputMessage: String): String {
        val chat = Chat(inputMessage)
        return try {
            thing.invokeAction(actionName = "ask", input = chat)
        } catch (e: Exception) {
            "Failed to receive an answer"
        }
    }
}