package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.spring.Agents
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AgentConfiguration {

    @Bean
    fun myAgent(agent: Agents) = agent {
        name = "My Agent"
        prompt { "you are a helpful weather agent." }
        model = { "GPT-4o" }
    }
}