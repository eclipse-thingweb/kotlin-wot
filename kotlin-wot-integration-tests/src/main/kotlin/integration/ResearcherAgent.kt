package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.agents.AgentProvider
import ai.ancf.lmos.arc.agents.ChatAgent
import ai.ancf.lmos.arc.agents.User
import ai.ancf.lmos.arc.agents.conversation.AssistantMessage
import ai.ancf.lmos.arc.agents.conversation.latest
import ai.ancf.lmos.arc.agents.conversation.toConversation
import ai.ancf.lmos.arc.agents.getAgentByName
import ai.ancf.lmos.arc.core.getOrThrow
import ai.ancf.lmos.wot.protocol.LMOSContext
import ai.ancf.lmos.wot.protocol.LMOSThingType
import ai.ancf.lmos.wot.reflection.annotations.Action
import ai.ancf.lmos.wot.reflection.annotations.Context
import ai.ancf.lmos.wot.reflection.annotations.Thing
import ai.ancf.lmos.wot.reflection.annotations.VersionInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.stereotype.Component


@Thing(id="researcher", title="Researcher Agent",
    description="A researcher agent.", type= LMOSThingType.AGENT)
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@VersionInfo(instance = "1.0.0")
@Component
class ResearcherAgent(agentProvider: AgentProvider, ) {

    private val messageFlow = MutableSharedFlow<String>(replay = 1) // Replay last emitted value

    val agent = agentProvider.getAgentByName("ResearcherAgent") as ChatAgent

    @Action(title = "Ask", description = "Ask the agent a question.")
    suspend fun ask(chat : Chat) : String {
        val assistantMessage = agent.execute(chat.message.toConversation(User("myId"))).getOrThrow().latest<AssistantMessage>() ?:
            throw RuntimeException("No Assistant response")
        return assistantMessage.content
    }
}

