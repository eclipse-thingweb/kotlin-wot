package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.spring.Agents
import ai.ancf.lmos.wot.Wot
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

    @Bean
    fun servient() = createServient("HTTP")

    @Bean
    fun wot() = Wot.create(servient())


}