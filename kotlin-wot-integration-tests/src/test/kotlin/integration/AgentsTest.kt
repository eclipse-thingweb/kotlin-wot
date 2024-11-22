package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolClientFactory
import ai.ancf.lmos.wot.thing.schema.toInteractionInputValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AgentsTest {

    @Test
    fun `Should talk to mqtt and http agent`() = runTest {

        val mqttConfig = MqttClientConfig("localhost", 54884, "wotClient")
        val servient =  Servient(
            clientFactories = listOf(
                HttpProtocolClientFactory(),
                MqttProtocolClientFactory(mqttConfig)),
        )
        servient.start()

        val wot = Wot.create(servient)

        val httpAgentTD = wot
            .requestThingDescription("http://localhost:8080/agent")

        val httpAgent = wot.consume(httpAgentTD)
        var output = httpAgent.invokeAction("ask",
            "What is Paris?".toInteractionInputValue())
        println(output.value())

        val mqttAgentTD = wot
            .requestThingDescription("mqtt://localhost:54884/agent")

        println(JsonMapper.instance.writeValueAsString(mqttAgentTD))

        val mqttAgent = wot.consume(mqttAgentTD)
        output = mqttAgent.invokeAction("ask",
            "What is London?".toInteractionInputValue())
        println(output.value())

    }
}
