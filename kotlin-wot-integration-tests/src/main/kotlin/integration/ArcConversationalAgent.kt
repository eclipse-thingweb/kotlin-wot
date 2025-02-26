package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.protocol.ConversationalAgent
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.SpanAttribute
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.eclipse.lmos.arc.agents.AgentProvider
import org.eclipse.lmos.arc.agents.User
import org.eclipse.lmos.arc.agents.conversation.AssistantMessage
import org.eclipse.lmos.arc.agents.conversation.latest
import org.eclipse.lmos.arc.agents.conversation.toConversation
import org.eclipse.lmos.arc.agents.getAgentByName
import org.eclipse.lmos.arc.core.getOrThrow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ArcConversationalAgent(agentProvider: AgentProvider) : ConversationalAgent<String, String> {

    private val agent = agentProvider.getAgentByName("ChatAgent") as org.eclipse.lmos.arc.agents.ChatAgent

    private val log : Logger = LoggerFactory.getLogger(ArcConversationalAgent::class.java)

    @WithSpan
    override suspend fun chat(@SpanAttribute message: String): String {
        log.info("Chat input: $message")
        val assistantMessage = agent.execute(message.toConversation(User("myId"))).getOrThrow().latest<AssistantMessage>()
            ?: throw RuntimeException("No Assistant response")

        val currentSpan = Span.current()
        currentSpan.setAttribute("assistantMessage", assistantMessage.content)
        return assistantMessage.content
    }
}