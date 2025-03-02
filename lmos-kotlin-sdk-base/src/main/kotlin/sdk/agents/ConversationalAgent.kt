package ai.ancf.lmos.sdk.agents

import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult
import ai.ancf.lmos.sdk.model.Message
import kotlin.reflect.KClass

interface ConversationalAgent {
    suspend fun chat(message: AgentRequest): AgentResult
}

interface ConsumedConversationalAgent: ConversationalAgent {
    suspend fun <T : Any> consumeEvent(eventName: String, clazz: KClass<T>, listener: EventListener<T>)
}

fun interface EventListener<T> {
    suspend fun handleEvent(data: T)
}

fun String.toAgentRequest(): AgentRequest {
    return AgentRequest(
        messages = listOf(
            Message(
                role = "user",
                content = this
            )
        )
    )
}

fun AgentResult.lastMessage(): String {
    return this.messages.last().content
}


