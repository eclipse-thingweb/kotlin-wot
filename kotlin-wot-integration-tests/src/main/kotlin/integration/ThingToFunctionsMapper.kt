package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.arc.agents.functions.LLMFunction
import ai.ancf.lmos.arc.agents.functions.ParameterSchema
import ai.ancf.lmos.arc.agents.functions.ParameterType
import ai.ancf.lmos.arc.spring.Functions
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.schema.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import org.slf4j.LoggerFactory
import java.math.BigDecimal

object ThingToFunctionsMapper {

    private val log = LoggerFactory.getLogger(ThingToFunctionsMapper::class.java)

    private val thingDescriptionsMap = mutableMapOf<String, WoTConsumedThing>()

    suspend fun exploreToolDirectory(wot: Wot, functions: Functions, url: String, securityScheme: SecurityScheme): List<LLMFunction> {
        val thingDescriptions = wot.exploreDirectory(url, securityScheme)
        val retrieveAllFunction = createRetrieveAllFunction(functions, thingDescriptions)
        val consumedThings = consumeThings(wot, thingDescriptions)
        return retrieveAllFunction + mapAllThingFunctions(functions, consumedThings)
    }

    private fun createRetrieveAllFunction(functions: Functions, thingDescriptions: Set<WoTThingDescription>): List<LLMFunction> {
        return functions("retrieveAllThings", "Returns the metadata information of all available devices.", "all_things") {
            summarizeThingDescriptions(thingDescriptions)
        }
    }

    private fun consumeThings(wot: Wot, thingDescriptions: Set<WoTThingDescription>): List<WoTConsumedThing> {
        return thingDescriptions.map { wot.consume(it) }.also { consumedThings ->
            thingDescriptionsMap.putAll(consumedThings.associateBy { it.getThingDescription().id })
        }
    }

    private fun mapAllThingFunctions(functions: Functions, consumedThings: List<WoTConsumedThing>): List<LLMFunction> {
        return consumedThings.flatMap { mapThingDescriptionToFunctions(functions, it) }
    }

    private fun summarizeThingDescriptions(things: Set<WoTThingDescription>): String {
        return things.joinToString("\n") { thing ->
            val types = thing.objectType?.types?.joinToString(", ") ?: "N/A"
            val actions = summarizeActions(thing)
            val properties = summarizeProperties(thing)
            """
            thingId: ${thing.id}, Title: ${thing.title ?: "N/A"}, Types: $types
            Actions:
            $actions
            Properties:
            $properties
            """.trimIndent()
        }
    }

    private fun summarizeActions(thing: WoTThingDescription): String {
        return thing.actions.entries.joinToString("\n    ") { (key, action) -> "$key: ${action.title} - ${action.description}" }
    }

    private fun summarizeProperties(thing: WoTThingDescription): String {
        return thing.properties.entries.joinToString("\n    ") { (key, property) -> "$key: ${property.title} - ${property.description}" }
    }

    private  fun mapThingDescriptionToFunctions(functions: Functions, thing: WoTConsumedThing): List<LLMFunction> {
        val thingDescription = thing.getThingDescription()
        val defaultParams = createDefaultParams()
        val actionFunctions = createActionFunctions(functions, thingDescription, defaultParams)
        val propertyFunctions = createPropertyFunctions(functions, thing, thingDescription, defaultParams)
        val readAllPropertiesFunction = createReadAllPropertiesFunction(functions, thing)
        return actionFunctions + propertyFunctions + readAllPropertiesFunction
    }

    private fun createDefaultParams(): List<Pair<ParameterSchema, Boolean>> {
        return listOf(Pair(ParameterSchema("thingId", "The unique identifier of the thing", ParameterType("string"), emptyList()), true))
    }

    private fun createActionFunctions(functions: Functions, thingDescription: WoTThingDescription, defaultParams: List<Pair<ParameterSchema, Boolean>>): List<LLMFunction> {
        return thingDescription.actions.flatMap { (actionName, action) ->
            val actionParams = action.input?.let { listOf(Pair(mapDataSchemaToParam(it), true)) } ?: emptyList()
            val params = defaultParams + actionParams
            functions(actionName, action.description ?: "No Description available", thingDescription.title, params) { (thingId, input) ->
                invokeAction(thingDescription.id, actionName, input, action.input)
            }
        }
    }

    private suspend fun invokeAction(thingId: String, actionName: String, input: String?, inputSchema: DataSchema<*>?): String {
        return try {
            val jsonInput : JsonNode = inputSchema?.let { mapSchemaToJsonNode(it, input!!) } ?: TextNode(input)
            thingDescriptionsMap[thingId]?.invokeAction(actionName, jsonInput)?.asText()
                ?: "Function call failed"
        } catch (e: Exception) {
            log.error("Error invoking action $actionName", e)
            "Function call failed"
        }
    }

    private fun createReadAllPropertiesFunction(functions: Functions, thing: WoTConsumedThing): List<LLMFunction> {
        return functions("readAllProperties", "Read all properties of a thing", "This function retrieves all properties of a thing") {
            thing.readAllProperties().map { (propertyName, futureValue) -> "$propertyName: ${futureValue.value().asText()}" }.joinToString("\n")
        }
    }

    private fun createPropertyFunctions(functions: Functions, thing: WoTConsumedThing, thingDescription: WoTThingDescription, defaultParams: List<Pair<ParameterSchema, Boolean>>): List<LLMFunction> {
        return thingDescription.properties.flatMap { (propertyName, property) ->
            when {
                property.readOnly -> createReadPropertyFunction(functions, thingDescription, propertyName)
                property.writeOnly -> createWritePropertyFunction(functions, thingDescription, propertyName, property, defaultParams)
                else -> createReadPropertyFunction(functions, thingDescription, propertyName) + createWritePropertyFunction(functions, thingDescription, propertyName, property, defaultParams)
            }
        }
    }

    private fun createReadPropertyFunction(
        functions: Functions,
        thingDescription: WoTThingDescription,
        propertyName: String
    ) = functions(
                "read_$propertyName",
                "Reads the value of the $propertyName property.",
                thingDescription.title
            ) { (thingId) ->
                try {
                    thingDescriptionsMap[thingId]?.readProperty(propertyName)?.value()?.asText() ?: "Function call failed"
                } catch (e: Exception) {
                    log.error("Error reading property $propertyName", e)
                    "Function call failed"
                }
            }

    private fun createWritePropertyFunction(
        functions: Functions,
        thingDescription: WoTThingDescription,
        propertyName: String,
        property: PropertyAffordance<*>,
        defaultParams: List<Pair<ParameterSchema, Boolean>>
    ) : List<LLMFunction> {
        val params = defaultParams + listOf(Pair(mapPropertyToParam(propertyName, property), true))
        return functions(
            "set_$propertyName",
            "Sets the value of the $propertyName property.",
            thingDescription.title,
            params
        ) { (thingId, propertyValue) ->
            if (propertyValue != null) {
                try {
                    val internalThingDescription = thingDescriptionsMap[thingId]?.getThingDescription()
                    val propertyAffordance = internalThingDescription?.properties?.get(propertyName)

                    if (internalThingDescription == null || propertyAffordance == null) {
                        return@functions "Function call failed"
                    }

                    thingDescriptionsMap[thingId]?.writeProperty(
                        propertyName,
                        mapSchemaToJsonNode(propertyAffordance, propertyValue)
                    )
                        ?: "Function failed"

                    "Property $propertyName set to $propertyValue"
                } catch (e: Exception) {
                    log.error("Error writing property $propertyName", e)
                    "Function call failed"
                }
            } else {
                "Function call failed"
            }
        }
    }

    private fun mapSchemaToJsonNode(schema: DataSchema<*>, value: String): JsonNode {
        return when (schema) {
            is StringSchema -> TextNode(value)
            is IntegerSchema -> IntNode(value.toIntOrNull() ?: 0)
            is NumberSchema -> DecimalNode(value.toBigDecimalOrNull() ?: BigDecimal.ZERO)
            is BooleanSchema -> BooleanNode.valueOf(value.toBooleanStrictOrNull() ?: false)
            else -> throw IllegalArgumentException("Unsupported schema type: ${schema::class.simpleName}")
        }
    }

    private fun mapPropertyToParam(propertyName : String, property: PropertyAffordance<*>): ParameterSchema {
        return when (property) {
            is StringSchema -> ParameterSchema(
                name = propertyName,
                description = property.description ?: "A string parameter",
                type = ParameterType("string"),
                enum = property.enum?.map { it.toString() } ?: emptyList()
            )
            is IntegerSchema -> ParameterSchema(
                name = propertyName,
                description = property.description ?: "An integer parameter",
                type = ParameterType("integer"),
                enum = property.enum?.map { it.toString() } ?: emptyList()
            )
            is NumberSchema -> ParameterSchema(
                name = propertyName,
                description = property.description ?: "A number parameter",
                type = ParameterType("number"),
                enum = property.enum?.map { it.toString() } ?: emptyList()
            )
            is BooleanSchema -> ParameterSchema(
                name = propertyName,
                description = property.description ?: "A boolean parameter",
                type = ParameterType("boolean"),
                enum = property.enum?.map { it.toString() } ?: emptyList()
            )
            is ArraySchema<*> -> ParameterSchema(
                name = propertyName,
                description = property.description ?: "An array parameter",
                type = mapDataSchemaToParamType(property.items!!),
                enum = property.enum?.map { it.toString() } ?: emptyList()
            )
            is ObjectSchema -> ParameterSchema(
                name = propertyName,
                description = property.description ?: "An object parameter",
                type = ParameterType("object", properties = property.properties.values.map(::mapDataSchemaToParameterSchema)),
                enum = property.enum?.map { it.toString() } ?: emptyList()
            )
            else -> throw IllegalArgumentException("Unsupported DataSchema type: ${property::class}")
        }
    }

    private fun mapDataSchemaToParamType(dataSchema: DataSchema<*>): ParameterType {
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

    private fun mapDataSchemaToParameterSchema(dataSchema: DataSchema<*>): ParameterSchema {
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
}
