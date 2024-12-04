package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.agents.AgentProvider
import ai.ancf.lmos.arc.agents.ChatAgent
import ai.ancf.lmos.arc.agents.conversation.AssistantMessage
import ai.ancf.lmos.arc.agents.conversation.Conversation
import ai.ancf.lmos.arc.agents.conversation.latest
import ai.ancf.lmos.arc.agents.getAgentByName
import ai.ancf.lmos.arc.core.getOrThrow
import ai.ancf.lmos.wot.reflection.annotations.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.springframework.stereotype.Component


@Thing(id= "agent", title="Agent",
    description= "A simple agent.")
@VersionInfo(instance = "1.0.0")
@Component
class ThingAgent(agentProvider: AgentProvider, @Property(name = "modelTemperature", readOnly = true)
                 val modelConfiguration: ModelConfiguration = ModelConfiguration(0.5, 50)) {

    private val messageFlow = MutableSharedFlow<String>(replay = 1) // Replay last emitted value

    val agent = agentProvider.getAgentByName("My Agent") as ChatAgent

    /*
    private val model: AzureOpenAiChatModel = AzureOpenAiChatModel.builder()
        .apiKey("af12dab9c046453e82dcf4b24af90bca")
        .deploymentName("GPT35T-1106")
        .endpoint("https://gpt4-uk.openai.azure.com/")
        .temperature(modelConfiguration.modelTemperature)
        .build();
     */

    @Property(name = "observableProperty", title = "Observable Property", readOnly = true)
    val observableProperty : MutableStateFlow<String> = MutableStateFlow("Hello World")

    @Action(name = "ask", title = "Ask", description = "Ask the agent a question.")
    suspend fun ask(conversation : Conversation) : String {
        val assistantMessage = agent.execute(conversation).getOrThrow().latest<AssistantMessage>() ?:
            throw RuntimeException("No Assistant response")
        messageFlow.emit(assistantMessage.content)
        return assistantMessage.content
    }

    @Event(name = "messageGenerated", title = "Generated message")
    fun messageGenerated() : Flow<String> {
        return messageFlow
    }
}

data class ModelConfiguration(val modelTemperature: Double, val maxTokens: Int)

