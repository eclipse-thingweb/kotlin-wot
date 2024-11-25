package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolServer
import ai.ancf.lmos.wot.reflection.annotations.ThingAgent
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.schema.*

private const val PROPERTY_NAME = "property1"
private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "ask"

private const val EVENT_NAME = "event1"

fun createServient(protocol: String): Servient {
    return when (protocol) {
        "HTTP" -> Servient(
            servers = listOf(HttpProtocolServer()),
            clientFactories = listOf(HttpProtocolClientFactory())
        )
        "MQTT" -> {
            val mqttConfig = MqttClientConfig("localhost", 54884, "wotServer")
            Servient(
                servers = listOf(MqttProtocolServer(mqttConfig))
            )
        }
        else -> throw IllegalArgumentException("Unsupported protocol: $protocol")
    }
}

fun createExposedThing(wot: Wot, agent: ThingAgent): ExposedThing {
    return wot.produce {
        id = "agent"
        title = "Agent"
        objectType = Type("Agent")
        intProperty(PROPERTY_NAME) { observable = true }
        intProperty(PROPERTY_NAME_2) { observable = true }
        action<String, Map<*, *>>(ACTION_NAME) {
            title = ACTION_NAME
            description = "Ask a question to the agent"
            input = stringSchema {
                title = "Action Input"
                description = "Question"
                minLength = 10
                default = "test"
            }
            output = objectSchema {
                stringProperty("response") {
                    description = "Answer of the agent"
                }
            }
        }
        event<String, Nothing, Nothing>(EVENT_NAME) { data = StringSchema() }
    }.apply {
        setPropertyReadHandler(PROPERTY_NAME) { 10.toInteractionInputValue() }
        setPropertyReadHandler(PROPERTY_NAME_2) { 5.toInteractionInputValue() }
        setPropertyWriteHandler(PROPERTY_NAME) { input, _ ->
            val inputInt = input.value() as DataSchemaValue.IntegerValue
            inputInt.value.toInteractionInputValue()
        }
        setActionHandler(ACTION_NAME) { input, _ ->
            val inputString = input.value() as DataSchemaValue.StringValue
            agent.ask(inputString.value).toInteractionInputValue()
        }
    }
}