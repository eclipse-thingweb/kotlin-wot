package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.form.Form
import app.cash.turbine.test
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test
import kotlin.test.assertEquals

class MqttProtocolClientTest {

    companion object {
        private lateinit var hiveMqContainer: GenericContainer<*>
        private lateinit var mqttClient: Mqtt5AsyncClient
        private lateinit var brokerUrl: String

        @BeforeAll
        @JvmStatic
        fun setUpContainer() {
            hiveMqContainer = GenericContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"))
                .withExposedPorts(1883)
            hiveMqContainer.start()

            brokerUrl = "mqtt://${hiveMqContainer.host}:${hiveMqContainer.getMappedPort(1883)}"
            mqttClient = Mqtt5Client.builder()
                .serverHost(hiveMqContainer.host)
                .serverPort(hiveMqContainer.getMappedPort(1883))
                .buildAsync()
        }

        @AfterAll
        @JvmStatic
        fun tearDownContainer() {
            hiveMqContainer.stop()
        }
    }

    private lateinit var client: MqttProtocolClient
    private lateinit var form: Form

    @BeforeEach
    fun setUp() = runTest {
        client = MqttProtocolClient(mqttClient, false)
        client.start()
    }

    @Test
    fun `invokeResource should publish null message to broker`() = runTest {
        form = Form("$brokerUrl/thingId/actions/actionName", "application/json")

        client.invokeResource(form)

        // Verify that the message was published
    }

    @Test
    fun `invokeResource with content should publish given content to broker`() = runTest {
        form = Form("$brokerUrl/thingId/actions/actionName", "application/json")
        val testMessage = "\"Hello World\""
        val expectedPayload = testMessage.toByteArray()
        val responseMessage = "\"Acknowledged\""
        val responsePayload = responseMessage.toByteArray()

        // Subscribe to the topic before publishing
        // Subscribe to the request topic and publish a response upon receiving the request
        launch {
            mqttClient.subscribeWith()
                .topicFilter("thingId/actions/actionName")
                .callback { publish ->
                    println("Received message on topic: ${publish.topic}")
                    val receivedPayload = publish.payloadAsBytes
                    assertEquals(
                        expectedPayload.contentToString(),
                        receivedPayload.contentToString(),
                        "Received payload on request topic did not match expected request payload"
                    )
                    // Publish a response message on the response topic
                    mqttClient.publishWith()
                        .topic(publish.responseTopic.get())
                        .payload(responsePayload)
                        .send()
                }
                .send().await()
        }

        // Publish the message using invokeResource
        val response = client.invokeResource(form, Content("application/json", expectedPayload))
        assertEquals("application/json", response.type)
        assertEquals("\"Acknowledged\"", response.body.decodeToString())
    }

    @Test
    fun `subscribeResource should subscribe to broker and emit content via Flow`() = runTest {
        client.subscribeResource(form).test {
            mqttClient.publish(Mqtt5Publish.builder()
                .topic("counter/events/change")
                .payload("Hello World".toByteArray())
                .build()).await()

            val item = awaitItem()
            assertEquals("Hello World", item.body.decodeToString())
            awaitComplete()
        }
    }

    @Test
    fun `subscribeResource should reuse existing broker subscriptions`() = runTest {
        val existingFlow = flowOf(Content("application/json", "Existing Data".toByteArray()))
        val topicSubjects = mutableMapOf("counter/events/change" to existingFlow)
        client = MqttProtocolClient(mqttClient, false)

        client.subscribeResource(form).test {
            val item = awaitItem()
            assertEquals("Existing Data", item.body.decodeToString())
            awaitComplete()
        }
    }

    @Test
    fun `subscribeResource should unsubscribe from broker when no more subscriptions`() = runTest {
        client.subscribeResource(form).test {
            cancelAndIgnoreRemainingEvents()
        }

        // Verify that the client unsubscribed from the topic
    }
}