package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.spring.Agents
import ai.ancf.lmos.arc.spring.Functions
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AgentConfiguration {

    @Bean
    fun myAgent(agent: Agents) = agent {
        name = "My Agent"
        prompt { "You are a helpful agent which can control devices." }
        model = { "GPT-4o" }
        tools {
            +"getResources"
        }
    }


    @Bean
    fun getResources(function: Functions, wot: Wot) = function(
        name = "getResources",
        description = "Returns the resources available in the coffee machine.",
    ) {

        val thingDescription =
            wot.requestThingDescription("http://remotelab.esi.cit.tum.de:8080/virtual-coffee-machine-1_1")

        val testThing = wot.consume(thingDescription)

        val availableResources = testThing.genericReadProperty<Resources>("allAvailableResources")

        """
            The coffee machine has the following resources available:
            - Milk: ${availableResources.milk} ml
            - Water: ${availableResources.water} ml
            - Chocolate: ${availableResources.chocolate} grams
            - Coffee Beans: ${availableResources.coffeeBeans} grams
        """
    }
}

data class Resources(
    val milk: Int,
    val water: Int ,
    val chocolate : Int,
    val coffeeBeans: Int
)