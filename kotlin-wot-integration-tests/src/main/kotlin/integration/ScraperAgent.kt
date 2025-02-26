package ai.ancf.lmos.wot.integration


import ai.ancf.lmos.wot.protocol.LMOSContext
import ai.ancf.lmos.wot.protocol.LMOSThingType
import ai.ancf.lmos.wot.reflection.annotations.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.eclipse.lmos.arc.agents.AgentProvider
import org.eclipse.lmos.arc.agents.User
import org.eclipse.lmos.arc.agents.conversation.AssistantMessage
import org.eclipse.lmos.arc.agents.conversation.latest
import org.eclipse.lmos.arc.agents.conversation.toConversation
import org.eclipse.lmos.arc.agents.getAgentByName
import org.eclipse.lmos.arc.core.getOrThrow
import org.springframework.stereotype.Component


@Thing(id="scraper", title="Scraper Agent",
    description="A scraper agent.", type= LMOSThingType.AGENT)
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@VersionInfo(instance = "1.0.0")
@Component
class ScraperAgent(agentProvider: AgentProvider) {

    private val messageFlow = MutableSharedFlow<String>(replay = 1) // Replay last emitted value

    val agent = agentProvider.getAgentByName("ScraperAgent") as org.eclipse.lmos.arc.agents.ChatAgent

    @Event(description = "HTML Content of the scraped web site")
    fun contentRetrieved() : Flow<String> {
        return messageFlow
    }

    @Action(title = "chat", description = "Ask the agent a question.")
    suspend fun chat(message: String) {
        val assistantMessage = agent.execute(message.toConversation(User("myId"))).getOrThrow().latest<AssistantMessage>() ?:
        throw RuntimeException("No Assistant response")
        messageFlow.emit(assistantMessage.content)
    }
}

