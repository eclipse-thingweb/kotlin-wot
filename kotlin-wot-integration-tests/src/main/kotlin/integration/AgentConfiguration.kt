package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.spring.Agents
import ai.ancf.lmos.arc.spring.Functions
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
    fun myFunction(function: Functions) = function(
        name = "g",
        description = "Returns real-time weather information for any location",
    ) {
        """
        The weather is good in Berlin. It is 20 degrees celsius.
    """
    }
}