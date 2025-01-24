package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolClientFactory
import ai.ancf.lmos.wot.thing.schema.toInteractionInputValue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AgentsTest {

    companion object {
        private lateinit var hiveMqContainer: GenericContainer<*>

        @BeforeAll
        @JvmStatic
        fun setUp() = runTest {
            hiveMqContainer = GenericContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"))
                .withExposedPorts(1883)
            hiveMqContainer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            hiveMqContainer.stop()
        }
    }

    private lateinit var servient: Servient

    @BeforeTest
    fun setup() = runTest {
        val mqttConfig = MqttClientConfig("localhost", 54884, "wotClient")
        servient = Servient(
            clientFactories = listOf(
                HttpProtocolClientFactory(),
                MqttProtocolClientFactory(mqttConfig)
            )
        )
        servient.start() // Start the Servient before each test
    }

    @AfterTest
    fun teardown() = runTest {
        servient.shutdown() // Ensure the Servient is stopped after each test
    }

    @Test
    fun `Should talk to mqtt and http agent`() = runTest {
        val wot = Wot.create(servient)

        // Test HTTP agent
        val httpAgentTD = wot.requestThingDescription("http://localhost:8080/agent")
        val httpAgent = wot.consume(httpAgentTD)
        var output = httpAgent.invokeAction("ask", "What is Paris?".toInteractionInputValue())
        println(output.value())

        // Test MQTT agent
        val mqttAgentTD = wot.requestThingDescription("mqtt://localhost:54884/agent")
        println(JsonMapper.instance.writeValueAsString(mqttAgentTD))

        val mqttAgent = wot.consume(mqttAgentTD)
        output = mqttAgent.invokeAction("ask", "What is London?".toInteractionInputValue())
        println(output.value())
    }
}
