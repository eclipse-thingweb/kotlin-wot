package ai.ancf.lmos.wot.integration


import ai.ancf.lmos.wot.JsonMapper
import org.eclipse.lmos.arc.agents.events.Event
import org.eclipse.lmos.arc.agents.events.EventHandler
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher

class ArcEventListener(private val applicationEventPublisher: ApplicationEventPublisher) : EventHandler<Event> {

    override fun onEvent(event: Event) {
        applicationEventPublisher.publishEvent(AgentEvent(JsonMapper.instance.writeValueAsString(event)))
    }
}

data class AgentEvent(val message: String) : ApplicationEvent(message)