package ai.ancf.lmos.sdk.agents

import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult
import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
import ai.ancf.lmos.wot.thing.ConsumedThing
import io.opentelemetry.instrumentation.annotations.WithSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


class WotConversationalAgent private constructor(private val thing : ConsumedThing) :
    ConsumedConversationalAgent{

    private val log : Logger = LoggerFactory.getLogger(ConversationalAgent::class.java)

    companion object {
        suspend fun create(wot: Wot, url: String): ConsumedConversationalAgent {
            return WotConversationalAgent(wot.consume(wot.requestThingDescription(url)) as ConsumedThing)
        }

        suspend fun create(url: String): ConsumedConversationalAgent {
            val wot = Wot.create(Servient(clientFactories = listOf(
                HttpProtocolClientFactory(),
                HttpsProtocolClientFactory(),
                WebSocketProtocolClientFactory()
            )))
            return create(wot, url)
        }
    }

    @WithSpan
    override suspend fun chat(message: AgentRequest): AgentResult {
        return try {
            thing.invokeAction(actionName = "chat", input = message)
        } catch (e: Exception) {
            log.error("Failed to receive an answer", e)
            throw e
        }
    }

    override suspend fun <T : Any> consumeEvent(eventName: String, clazz: KClass<T>, listener: EventListener<T>, ) {
        thing.subscribeEvent(eventName, listener = { event ->
            try{
                val parsedEvent = JsonMapper.instance.treeToValue(event.value(), clazz.java)
                // Pass it to the listener
                listener.handleEvent(parsedEvent)
            } catch (e: Exception) {
                log.error("Failed to parse event", e)
                throw e
            }
        })
    }

    override suspend fun <T : Any> consumeEvent(eventName: String, clazz: KClass<T>): Flow<T> {
        return thing.consumeEvent(eventName).map { event ->
            try {
                JsonMapper.instance.treeToValue(event.value(), clazz.java)
            } catch (e: Exception) {
                log.error("Failed to parse event", e)
                throw e
            }
        }
    }
}