/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.integration


import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.integration.ThingToFunctionsMapper.mapDataSchemaToParam
import ai.ancf.lmos.wot.integration.ThingToFunctionsMapper.mapSchemaToJsonNode
import ai.ancf.lmos.wot.security.NoSecurityScheme
import ai.ancf.lmos.wot.thing.schema.WoTConsumedThing
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.runBlocking
import org.eclipse.lmos.arc.agents.dsl.AllTools
import org.eclipse.lmos.arc.agents.functions.LLMFunction
import org.eclipse.lmos.arc.spring.Agents
import org.eclipse.lmos.arc.spring.Functions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@EnableConfigurationProperties(ToolProperties::class)
class AgentConfiguration {

    private lateinit var thingDescriptionsMap: Map<String, WoTConsumedThing>

    private val log: Logger = LoggerFactory.getLogger(AgentConfiguration::class.java)

    @Bean
    fun chatArcAgent(agent: Agents) = agent {
        name = "ChatAgent"
        prompt {
            """
            You are a professional smart home assistant.  
            ## Instructions  

            - Always perform the steps stated in the "Steps" associated with the solution, if any, before providing the solution.  
            ---

            ### UseCase: control_smart_home_device  
            #### Description  
            Customer wants to control a smart home device (e.g., turn on/off lights, adjust thermostat, lock doors).  

            #### Steps  
            1. Ask the customer which device they want to control.  
            2.Identify the devices that match the customer’s request based on their types, properties and actions.
            3. Execute the requested action on the identified device(s) using the corresponding thingId.
            4. If multiple devices match the request, execute the action on all relevant devices.
            5. Confirm the action has been completed and provide a success message to the customer.

            #### Solution  
            1. Use the retrieveAllThings function to obtain metadata on all available devices.
            2. Identify devices that match the requested type and capabilities.
            3. Extract the thingId for each relevant device.
            4. Execute the control function by passing the appropriate thingId and required parameters.
            5. If an individual function call fails, continue executing the remaining actions until the request is fulfilled.
            6. Ensure that only successful operations are reported back to the customer.

            #### Fallback Solution  
            If the device is unresponsive, guide the customer to restart the device and check its connection. If the issue persists, escalate to a higher tier of support.  

            #### Examples  
            - "Turn on the living room lights."  
            - "Set the thermostat to 72°F."  
            - "Lock the front door."  
            
        """.trimIndent()
        }
        model = { "GPT-4o" }
        filterInput { -"Hello world" }
        tools = listOf("devices")
    }

    @Bean
    fun researcherArcAgent(agent: Agents) = agent {
        name = "ResearcherAgent"
        prompt { "You can do create a webpage summary based on HTML content." }
        model = { "GPT-4o" }
    }

    @Bean
    fun scraperArcAgent(agent: Agents) = agent {
        name = "ScraperAgent"
        prompt { "You can scrape a page by using the scraper tool." }
        model = { "GPT-4o" }
        tools = AllTools
    }

    @Bean
    fun agentEventListener(applicationEventPublisher: ApplicationEventPublisher) =
        ArcEventListener(applicationEventPublisher)


    /*

    @Bean
    fun discoverTools(toolProperties: ToolProperties, functions: Functions, wot: Wot) : List<LLMFunction> = runBlocking {
        toolProperties.tools.flatMap {
            log.info("Discovering tools for ${it.key}")
            log.info("Security scheme: ${it.value.securityScheme}")
            val json = """
                    {
                        "scheme": "${it.value.securityScheme}"
                    }
                    """
            val securityScheme = JsonMapper.instance.readValue<SecurityScheme>(json)
            val createdFunctions = if (it.value.isDirectory) {
                ThingToFunctionsMapper.exploreToolDirectory(wot, functions, it.key, it.value.url, securityScheme)
            } else {
                ThingToFunctionsMapper.requestThingDescription(wot, functions, it.key, it.value.url, securityScheme)
            }
            log.info("Added ${createdFunctions.size} functions to group ${it.key}")
            createdFunctions
        }
    }

     */

    @Bean
    fun scraperToolFunctions(toolProperties: ToolProperties, functions: Functions, wot: Wot, scraperTool: ScraperTool) : List<LLMFunction> = runBlocking {

        val thingDescription = wot.requestThingDescription(toolProperties.tools["scraper"]?.url!!, NoSecurityScheme())
        val consumedThing = wot.consume(thingDescription)

         thingDescription.actions.flatMap { (actionName, action) ->
            val actionParams = action.input?.let { listOf(Pair(mapDataSchemaToParam(it), true)) } ?: emptyList()
             functions(actionName, action.description ?: "No Description available", "scraper", actionParams) { input ->
                 try {
                     val jsonInput : JsonNode = action.output?.let { mapSchemaToJsonNode(it, input.first()!!) } ?: TextNode(input.first()!!)
                     consumedThing.invokeAction(actionName, jsonInput).asText()
                         ?: "Function call failed"
                 } catch (e: Exception) {
                     "Function call failed"
                 }
             }
        }

    }

    /*
    @Bean
    fun discoverLocalTools(functions: Functions, wot: Wot, scraperTool: ScraperTool) : List<LLMFunction>{
        log.info("Map Scraper to LLM Functions")
        val exposedThing = ExposedThingBuilder.createExposedThing(wot, scraperTool, ScraperTool::class)
        return if(exposedThing != null) {
            ThingToFunctionsMapper
                .mapThingDescriptionToFunctions2(functions, "scraper",
                    exposedThing.getThingDescription()).toList()
        }else{
            emptyList()
        }
    }
    */

}
