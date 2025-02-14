package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.agents.dsl.AllTools
import ai.ancf.lmos.arc.agents.functions.LLMFunction
import ai.ancf.lmos.arc.agents.functions.ParameterSchema
import ai.ancf.lmos.arc.agents.functions.ParameterType
import ai.ancf.lmos.arc.spring.Agents
import ai.ancf.lmos.arc.spring.Functions
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.security.BearerSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.schema.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal


@Configuration
class AgentConfiguration {

    private lateinit var thingDescriptionsMap : Map<String, WoTConsumedThing>

    private val log : Logger = LoggerFactory.getLogger(AgentConfiguration::class.java)

    @Bean
    fun chatArcAgent(agent: Agents) = agent {
        name = "ChatAgent"
        prompt { "You are a helpful smart home agent that can control devices. But never print thingIds to the customer" }
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
        /*
        discoverTool(wot, functions, "https://plugfest.webthings.io/things/virtual-things-2",
            BearerSecurityScheme())
        */
        exploreToolDirectory(wot, functions, "https://plugfest.webthings.io/.well-known/wot",
            BearerSecurityScheme())
    }

    private suspend fun exploreToolDirectory(wot: Wot, functions: Functions, url : String,
                                     securityScheme: SecurityScheme) : List<LLMFunction> {
        val thingDescriptions = wot.exploreDirectory(url, securityScheme)

        val retrieveAllFunction = functions(
            "retrieveAllThings",
            "Retrieves the metadata information of all things/devices available. " +
                    "Can be used to understand which device types are available and retrieve the " +
                    "thingIds to control multiple devices. The types tell you the capabilities of a device.",
            "all_things"
        ) {
            summarizeThingDescriptions(thingDescriptions)
        }

        val consumedThings = thingDescriptions.map { wot.consume(it) }
        thingDescriptionsMap = consumedThings.associateBy { it.getThingDescription().id }

        return retrieveAllFunction + consumedThings
            .flatMap { mapThingDescriptionToFunctions(functions, it) }
    }

    fun summarizeThingDescriptions(things: Set<WoTThingDescription>): String {
        return things.joinToString(separator = "\n") { thing ->
            val types = thing.objectType?.types?.joinToString(", ") ?: "N/A"
            "thingId: ${thing.id}, Title: ${thing.title ?: "N/A"}, Types: $types"
        }
    }

    private suspend fun discoverTool(wot: Wot, functions: Functions, url : String,
                                     securityScheme: SecurityScheme) : List<LLMFunction> {
        val thingDescription =
            wot.requestThingDescription(url, securityScheme)

        val thing = wot.consume(thingDescription)

        return mapThingDescriptionToFunctions(functions, thing)
    }

    private suspend fun mapThingDescriptionToFunctions(
        functions: Functions,
        thing: WoTConsumedThing
    ): List<LLMFunction> {
        val thingDescription = thing.getThingDescription()

        val defaultParams = listOf(Pair(ParameterSchema(
            name = "thingId",
            description = "The unique identifier of the thing",
            type = ParameterType("string"),
            enum = emptyList()
        ), true))

        val actionFunctions = thingDescription.actions.flatMap { (actionName, action) ->

            val actionParams = action.input?.let { input ->
                listOf(Pair(mapDataSchemaToParam(input), true))
            } ?: emptyList()

            val params = defaultParams + actionParams

            functions(
                actionName,
                action.description ?: "No Description available",
                thingDescription.title,
                params,
            ) {
                (thingId, input) ->
                try {
                    thingDescriptionsMap[thingId]?.invokeAction(actionName, TextNode(input))?.asText()?: "Function call failed"
                }catch (e: Exception) {
                    log.error("Error invoking action $actionName", e)
                    "Function call failed"
                }
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
                    (thingId) ->
                    try {
                        thingDescriptionsMap[thingId]?.readProperty(propertyName)?.value()?.asText() ?: "Function call failed"
                    } catch (e: Exception) {
                        log.error("Error reading property $propertyName", e)
                        "Function call failed"
                    }
                }
            } else if (property.writeOnly) {
                val params = defaultParams + listOf(Pair(mapDataSchemaToParam(property), true))

                functions(
                    "set$propertyName",
                    property.description ?: "Can be used to set the $propertyName property",
                    thingDescription.title,
                    params
                ) {
                    (thingId, propertyValue) ->
                    if(propertyValue != null){
                        try {
                            val propertyAffordance = thing.getThingDescription().properties[propertyName]!!
                            thingDescriptionsMap[thingId]?.writeProperty(propertyName, mapSchemaToJsonNode(propertyAffordance, propertyValue))  ?: "Function failed"
                            "Property $propertyName set to $propertyValue"
                        } catch (e: Exception) {
                            log.error("Error writing property $propertyName", e)
                            "Function call failed"
                        }
                    }else{
                        "Function call failed"
                    }
                }
            } else {
                functions(
                    "read$propertyName",
                    property.description ?: "Can be used to read the $propertyName property",
                    thingDescription.title
                ) {
                    (thingId) ->
                    try {
                        thingDescriptionsMap[thingId]?.readProperty(propertyName)?.value()?.asText() ?: "Function failed"
                    } catch (e: Exception) {
                        log.error("Error reading property $propertyName", e)
                        "Function call failed"
                    }
                }
                val params = defaultParams + listOf(Pair(mapDataSchemaToParam(property), true))
                functions(
                    "set$propertyName",
                    property.description ?: "Can be used to set the $propertyName property",
                    thingDescription.title,
                    params
                ) {
                    (thingId, propertyValue) ->
                    if(propertyValue != null){
                        try {
                            val propertyAffordance = thing.getThingDescription().properties[propertyName]!!
                            thingDescriptionsMap[thingId]?.writeProperty(propertyName, mapSchemaToJsonNode(propertyAffordance, propertyValue))  ?: "Function failed"
                            "Property $propertyName set to $propertyValue"
                        } catch (e: Exception) {
                            log.error("Error writing property $propertyName", e)
                            "Function call failed"
                        }
                    }else{
                        "Function call failed"
                    }
                }
            }
        }
        return actionFunctions + propertyFunctions + propertiesFunctions
    }

    fun mapSchemaToJsonNode(schema: DataSchema<*>, value: String): JsonNode {
        return when (schema) {
            is StringSchema -> TextNode(value)
            is IntegerSchema -> {
                val intValue = value.toIntOrNull() ?: 0
                IntNode(intValue)
            }
            is NumberSchema -> {
                val numberValue = value.toBigDecimalOrNull() ?: BigDecimal.ZERO
                DecimalNode(numberValue)
            }
            is BooleanSchema -> {
                val boolValue = value?.toBooleanStrictOrNull() ?: false
                BooleanNode.valueOf(boolValue)
            }
            else -> throw IllegalArgumentException("Unsupported schema type: ${schema::class.simpleName}")
        }
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

