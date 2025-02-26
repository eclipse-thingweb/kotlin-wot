package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.protocol.ConversationalAgent
import ai.ancf.lmos.wot.protocol.LMOSContext
import ai.ancf.lmos.wot.protocol.LMOSThingType
import ai.ancf.lmos.wot.reflection.annotations.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component


@Thing(id="chatagent", title="Chat Agent",
    description="A chat agent.", type= LMOSThingType.AGENT)
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@Link(href = "lmos/capabilities", rel = "service-meta", type = "application/json")
@VersionInfo(instance = "1.0.0")
@Component
class ChatAgent(private val arcAgent: ConversationalAgent<String, String>): ApplicationListener<AgentEvent> {

    private val agentEventFlow = MutableSharedFlow<String>(replay = 1) // Replay last emitted value

    @Action(title = "Chat", description = "Ask the agent a question.")
    @ActionInput(title = "The question", description = "A question")
    @ActionOutput(title = "The question", description = "A question")
    suspend fun chat(message: String) : String {
        return arcAgent.chat(message)
    }

    @Event(title = "Agent Event", description = "An event from the agent.")
    fun agentEvent() : Flow<String> {
        return agentEventFlow
    }

    override fun onApplicationEvent(event: AgentEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            agentEventFlow.emit(JsonMapper.instance.writeValueAsString(event))
        }
    }
}


