/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.integration


import ai.ancf.lmos.sdk.model.AgentEvent
import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult
import ai.ancf.lmos.wot.protocol.LMOSContext
import ai.ancf.lmos.wot.protocol.LMOSThingType
import ai.ancf.lmos.wot.reflection.annotations.*
import integration.executeAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.eclipse.lmos.arc.agents.AgentProvider
import org.eclipse.lmos.arc.agents.getAgentByName
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component


@Thing(id="scraper", title="Scraper Agent",
    description="A scraper agent.", type= LMOSThingType.AGENT)
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@VersionInfo(instance = "1.0.0")
@Component
class ScraperAgent(agentProvider: AgentProvider) : ApplicationListener<SpringApplicationAgentEvent> {

    private val agentEventFlow = MutableSharedFlow<AgentEvent>()

    val agent = agentProvider.getAgentByName("ScraperAgent") as org.eclipse.lmos.arc.agents.ChatAgent

    @Action(title = "chat", description = "Ask the agent a question.")
    suspend fun chat(message: AgentRequest) : AgentResult {
        return executeAgent(message, agent::execute)
    }

    @Event(title = "Agent Event", description = "An event from the agent.")
    fun agentEvent() : Flow<AgentEvent> {
        return agentEventFlow
    }

    override fun onApplicationEvent(event: SpringApplicationAgentEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            agentEventFlow.emit(event.event)
        }
    }
}

