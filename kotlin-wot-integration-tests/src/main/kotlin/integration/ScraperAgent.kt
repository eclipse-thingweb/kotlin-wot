package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.agents.AgentProvider
import ai.ancf.lmos.arc.agents.ChatAgent
import ai.ancf.lmos.arc.agents.User
import ai.ancf.lmos.arc.agents.conversation.AssistantMessage
import ai.ancf.lmos.arc.agents.conversation.latest
import ai.ancf.lmos.arc.agents.conversation.toConversation
import ai.ancf.lmos.arc.agents.getAgentByName
import ai.ancf.lmos.arc.core.getOrThrow
import ai.ancf.lmos.wot.protocol.ConversationalAgent
import ai.ancf.lmos.wot.protocol.LMOSContext
import ai.ancf.lmos.wot.protocol.LMOSThingType
import ai.ancf.lmos.wot.reflection.annotations.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.stereotype.Component


@Thing(id="scraper", title="Scraper Agent",
    description="A scraper agent.", type= LMOSThingType.AGENT)
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@VersionInfo(instance = "1.0.0")
@Component
class ScraperAgent(agentProvider: AgentProvider) : ConversationalAgent<String, Unit> {

    private val messageFlow = MutableSharedFlow<String>(replay = 1) // Replay last emitted value

    val agent = agentProvider.getAgentByName("ScraperAgent") as ChatAgent


    @Event(description = "HTML Content of the scraped web site")
    fun contentRetrieved() : Flow<String> {
        return messageFlow
    }

    @Action(title = "chat", description = "Ask the agent a question.")
    override suspend fun chat(message: String) {
        val assistantMessage = agent.execute(message.toConversation(User("myId"))).getOrThrow().latest<AssistantMessage>() ?:
        throw RuntimeException("No Assistant response")
        messageFlow.emit(assistantMessage.content)
    }
}

