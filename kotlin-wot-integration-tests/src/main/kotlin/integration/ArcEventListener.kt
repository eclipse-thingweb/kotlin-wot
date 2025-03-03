/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.integration



import ai.ancf.lmos.sdk.model.AgentEvent
import ai.ancf.lmos.wot.JsonMapper
import org.eclipse.lmos.arc.agents.events.Event
import org.eclipse.lmos.arc.agents.events.EventHandler
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher

class ArcEventListener(private val applicationEventPublisher: ApplicationEventPublisher) : EventHandler<Event> {

    override fun onEvent(event: Event) {
        applicationEventPublisher.publishEvent(SpringApplicationAgentEvent(
            AgentEvent(
                event::class.simpleName.toString(),
                JsonMapper.instance.writeValueAsString(event),
                event.context["conversationId"],
                event.context["turnId"])
        ))
    }
}

data class SpringApplicationAgentEvent(val event: AgentEvent) : ApplicationEvent(event)