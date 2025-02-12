package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.agents.dsl.AllTools
import ai.ancf.lmos.arc.agents.functions.LLMFunction
import ai.ancf.lmos.arc.agents.functions.ParameterSchema
import ai.ancf.lmos.arc.agents.functions.ParameterType
import ai.ancf.lmos.arc.spring.Agents
import ai.ancf.lmos.arc.spring.Functions
import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.security.BearerSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.schema.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AgentConfiguration {

    @Bean
    fun chatArcAgent(agent: Agents) = agent {
        name = "ChatAgent"
        prompt { "You are a helpful smart home agent that can control devices." }
        model = { "GPT-4o" }
        tools = AllTools
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
        prompt { "You can retrieve content by scraping a given URL." }
        model = { "GPT-4o" }
        tools = AllTools
    }


    @Bean
    fun discoverTools(functions: Functions, wot: Wot) : List<LLMFunction> = runBlocking {
        //discoverTool(wot, functions, "http://localhost:8081/scraper")
        discoverTool(wot, functions, "https://plugfest.webthings.io/things/virtual-things-2",
            BearerSecurityScheme())
    }

    private suspend fun discoverTool(wot: Wot, functions: Functions, url : String,
                                     securityScheme: SecurityScheme) : List<LLMFunction> {
        val thingDescription =
            wot.requestThingDescription(url, securityScheme)

        val thing = wot.consume(thingDescription)

        return mapThingDescriptionToFunctions(thingDescription, functions, thing)
    }

    private suspend fun mapThingDescriptionToFunctions(
        thingDescription: WoTThingDescription,
        functions: Functions,
        thing: WoTConsumedThing
    ): List<LLMFunction> {
        val actionFunctions = thingDescription.actions.flatMap { (actionName, action) ->

            val params = action.input?.let { input ->
                listOf(Pair(mapDataSchemaToParam(input), true))
            }

            functions(
                actionName,
                action.description ?: "No Description available",
                thingDescription.title,
                params ?: emptyList(),
            ) {
                (url) ->
                thing.invokeAction(actionName, TextNode(url)).asText()
            }
        }
        val propertiesFunctions = functions(
            "readAllProperties",
            "Read all properties of a thing",
            "This function retrieves all properties of a thing"
        ) {
            thing.readAllProperties().map { (propertyName, futureValue) ->
                "$propertyName: ${futureValue.value().asText()}"
            }.joinToString("\n")
        }

        val propertyFunctions = thingDescription.properties.flatMap { (propertyName, property) ->
            if (property.readOnly) {
                functions(
                    "read$propertyName",
                    property.description ?: "Can be used to read the $propertyName property",
                    thingDescription.title
                ) {
                    thing.readProperty(propertyName).value().asText()
                }
            } else if (property.writeOnly) {
                functions(
                    "set$propertyName",
                    property.description ?: "Can be used to set the $propertyName property",
                    thingDescription.title,
                    listOf(Pair(mapDataSchemaToParam(property), true))
                ) {
                    (propertyValue) ->
                    thing.writeProperty(propertyName, TextNode(propertyValue))
                    "Property $propertyName set to $propertyValue"
                }
            } else {
                functions(
                    "read$propertyName",
                    property.description ?: "Can be used to read the $propertyName property",
                    thingDescription.title
                ) {
                    thing.readProperty(propertyName).value().asText()
                }
                functions(
                    "set$propertyName",
                    property.description ?: "Can be used to set the $propertyName property",
                    thingDescription.title,
                    listOf(Pair(mapDataSchemaToParam(property), true))
                ) {
                        (propertyValue) ->
                    if(propertyValue != null){
                        val input : JsonNode = JsonMapper.instance.convertValue(propertyValue)
                        thing.writeProperty(propertyName, input)
                        "Property $propertyName set to $propertyValue"
                    }else{
                        "Function call failed"
                    }
                }
            }
        }
        return actionFunctions + propertyFunctions + propertiesFunctions
    }



    /*

    @Bean
    fun getResources(function: Functions, wot: Wot) = function(
        name = "getResources",
        description = "Returns the resources available in the coffee machine.",
    ) {

        val thingDescription =
            wot.requestThingDescription("http://plugfest.thingweb.io/http-data-schema-thing")

        val testThing = wot.consume(thingDescription)

        val availableResources = testThing.genericReadProperty<String>("int")

        availableResources

        /*
        """
            The coffee machine has the following resources available:
            - Milk: ${availableResources.milk} ml
            - Water: ${availableResources.water} ml
            - Chocolate: ${availableResources.chocolate} grams
            - Coffee Beans: ${availableResources.coffeeBeans} grams
        """
        */
    }
    */
}

data class Resources(
    val milk: Int,
    val water: Int ,
    val chocolate : Int,
    val coffeeBeans: Int
)

fun mapDataSchemaToParam(dataSchema: DataSchema<*>): ParameterSchema {
    return when (dataSchema) {
        is StringSchema -> ParameterSchema(
            name = "stringParam",
            description = "A string parameter",
            type = ParameterType("string"),
            enum = emptyList()
        )
        is IntegerSchema -> ParameterSchema(
            name = "integerParam",
            description = "An integer parameter",
            type = ParameterType("integer"),
            enum = emptyList()
        )
        is NumberSchema -> ParameterSchema(
            name = "numberParam",
            description = "A number parameter",
            type = ParameterType("number"),
            enum = emptyList()
        )
        is BooleanSchema -> ParameterSchema(
            name = "booleanParam",
            description = "A boolean parameter",
            type = ParameterType("boolean"),
            enum = emptyList()
        )
        is ArraySchema<*> -> ParameterSchema(
            name = "arrayParam",
            description = "An array parameter",
            type = mapDataSchemaToParamType(dataSchema.items!!),
            enum = emptyList()
        )
        is ObjectSchema -> ParameterSchema(
            name = "objectParam",
            description = "An object parameter",
            type = ParameterType("object", properties = dataSchema.properties.values.map(::mapDataSchemaToParameterSchema)),
            enum = emptyList()
        )
        else -> throw IllegalArgumentException("Unsupported DataSchema type: ${dataSchema::class}")
    }
}

fun mapDataSchemaToParamType(dataSchema: DataSchema<*>): ParameterType {
    return when (dataSchema) {
        is StringSchema -> ParameterType("string")
        is IntegerSchema -> ParameterType("integer")
        is NumberSchema -> ParameterType("number")
        is BooleanSchema -> ParameterType("boolean")
        is ArraySchema<*> -> {
            val itemType = dataSchema.items?.let { mapDataSchemaToParamType(it) }
            ParameterType("array", items = itemType)
        }
        is ObjectSchema -> {
            val properties = dataSchema.properties.values.map { mapDataSchemaToParamType(it) }
            ParameterType("object", properties = dataSchema.properties.values.map(::mapDataSchemaToParameterSchema))
        }
        else -> throw IllegalArgumentException("Unsupported DataSchema type: ${dataSchema::class}")
    }
}

fun mapDataSchemaToParameterSchema(dataSchema: DataSchema<*>): ParameterSchema {
    return when (dataSchema) {
        is StringSchema -> ParameterSchema(
            name = dataSchema.title ?: "No title",
            description = dataSchema.description ?: "No description",
            type = mapDataSchemaToParamType(dataSchema),
            enum = emptyList()
        )
        is IntegerSchema -> ParameterSchema(
            name = "integerParam",
            description = "An integer parameter",
            type = mapDataSchemaToParamType(dataSchema),
            enum = emptyList()
        )
        is NumberSchema -> ParameterSchema(
            name = "numberParam",
            description = "A number parameter",
            type = mapDataSchemaToParamType(dataSchema),
            enum = emptyList()
        )
        is BooleanSchema -> ParameterSchema(
            name = "booleanParam",
            description = "A boolean parameter",
            type =mapDataSchemaToParamType(dataSchema),
            enum = emptyList()
        )
        is ArraySchema<*> -> {
            val itemParameter = dataSchema.items?.let { mapDataSchemaToParameterSchema(it) }
            ParameterSchema(
                name = "arrayParam",
                description = "An array parameter",
                type = mapDataSchemaToParamType(dataSchema),
                enum = emptyList()
            ).apply {
                // Add item parameters if available
                itemParameter?.let { item ->
                    // Handle item parameters here
                }
            }
        }
        is ObjectSchema -> {
            val propertyParameters = dataSchema.properties?.values?.map { mapDataSchemaToParameterSchema(it) } ?: emptyList()
            ParameterSchema(
                name = "objectParam",
                description = "An object parameter",
                type = mapDataSchemaToParamType(dataSchema),
                enum = emptyList()
            ).apply {
                // Add property parameters if available
                // Handle property parameters here
            }
        }
        else -> throw IllegalArgumentException("Unsupported DataSchema type: ${dataSchema::class}")
    }
}

