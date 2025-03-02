package integration

import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult
import ai.ancf.lmos.sdk.model.Message
import org.eclipse.lmos.arc.agents.AgentFailedException
import org.eclipse.lmos.arc.agents.User
import org.eclipse.lmos.arc.agents.conversation.AssistantMessage
import org.eclipse.lmos.arc.agents.conversation.Conversation
import org.eclipse.lmos.arc.agents.conversation.latest
import org.eclipse.lmos.arc.agents.conversation.toConversation
import org.eclipse.lmos.arc.core.Result
import org.eclipse.lmos.arc.core.getOrThrow

suspend fun executeAgent(message: AgentRequest, function: suspend (Conversation) -> Result<Conversation, AgentFailedException>): AgentResult {
    val lastMessage : String = message.messages.last().content
    val assistantMessage = function(lastMessage.toConversation(User("myId"))).getOrThrow().latest<AssistantMessage>()
        ?: throw RuntimeException("No Assistant response")
    return AgentResult(
        messages = listOf(
            Message(
                role = "assistant",
                content = assistantMessage.content,
                turnId = assistantMessage.turnId
            )
        )
    )
}

