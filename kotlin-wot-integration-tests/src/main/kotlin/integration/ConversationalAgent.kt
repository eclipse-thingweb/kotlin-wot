package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
import ai.ancf.lmos.wot.protocol.ConsumedConversationalAgent
import ai.ancf.lmos.wot.protocol.ConversationalAgent
import ai.ancf.lmos.wot.protocol.EventListener
import ai.ancf.lmos.wot.thing.ConsumedThing
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class WotConversationalAgent private constructor(private val thing : ConsumedThing) :
    ConsumedConversationalAgent<String, String, String> {

    private val log : Logger = LoggerFactory.getLogger(ConversationalAgent::class.java)

    companion object {
        suspend fun create(wot: Wot, url: String): ConsumedConversationalAgent<String, String, String> {
            return WotConversationalAgent(wot.consume(wot.requestThingDescription(url)) as ConsumedThing)
        }

        suspend fun create(url: String): ConsumedConversationalAgent<String, String, String> {
            val wot = Wot.create(Servient(clientFactories = listOf(HttpProtocolClientFactory(), HttpsProtocolClientFactory(), WebSocketProtocolClientFactory())))
            return create(wot, url)
        }
    }

    override suspend fun chat(message: String): String {
        return try {
            thing.invokeAction(actionName = "chat", input = message)
        } catch (e: Exception) {
            log.error("Failed to receive an answer", e)
            "Failed to receive an answer"
        }
    }

    override suspend fun consumeEvent(eventName: String, listener: EventListener<String>) {
        thing.subscribeEvent(eventName, { listener.handleEvent(it.value().asText()) })
    }
}