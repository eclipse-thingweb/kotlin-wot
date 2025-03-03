/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.integration


import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult
import ai.ancf.lmos.wot.protocol.LMOSContext
import ai.ancf.lmos.wot.protocol.LMOSThingType
import ai.ancf.lmos.wot.reflection.annotations.Action
import ai.ancf.lmos.wot.reflection.annotations.Context
import ai.ancf.lmos.wot.reflection.annotations.Thing
import ai.ancf.lmos.wot.reflection.annotations.VersionInfo
import integration.executeAgent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.eclipse.lmos.arc.agents.AgentProvider
import org.eclipse.lmos.arc.agents.getAgentByName
import org.springframework.stereotype.Component


@Thing(id="researcher", title="Researcher Agent",
    description="A researcher agent.", type= LMOSThingType.AGENT)
@Context(prefix = LMOSContext.prefix, url = LMOSContext.url)
@VersionInfo(instance = "1.0.0")
@Component
class ResearcherAgent(agentProvider: AgentProvider) {

    private val messageFlow = MutableSharedFlow<String>(replay = 1) // Replay last emitted value

    val agent = agentProvider.getAgentByName("ResearcherAgent") as org.eclipse.lmos.arc.agents.ChatAgent

    @Action(title = "Chat", description = "Ask the agent a question.")
    suspend fun chat(message : AgentRequest) : AgentResult {
        return executeAgent(message, agent::execute)
    }
}

