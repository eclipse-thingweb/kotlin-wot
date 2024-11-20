package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.form.Form
import app.cash.turbine.test
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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

            val host = hiveMqContainer.host
            val mappedPort = hiveMqContainer.getMappedPort(1883)

            brokerUrl = "mqtt://$host:$mappedPort"
            mqttClient = Mqtt5Client.builder()
                .serverHost(host)
                .serverPort(mappedPort)
                .buildAsync()
        }

        @AfterAll
        @JvmStatic
        fun tearDownContainer() {
            hiveMqContainer.stop()
        }
    }

    private lateinit var client: MqttProtocolClient

    @BeforeTest
    fun setUp() = runTest {
        client = MqttProtocolClient(mqttClient, false)
        client.start()
    }
    @AfterTest
    fun tearDown() = runTest {
        client.stop()
    }

    @Test
    fun `invokeResource should publish null message to broker`() = runTest {
        val form = Form("$brokerUrl/thingId/actions/actionName", "application/json")

        // Subscribe to the topic before publishing

        mqttClient.subscribeWith()
            .topicFilter("thingId/actions/actionName")
            .callback { publish ->
                println("Received message on topic: ${publish.topic}")
                val receivedPayload = publish.payloadAsBytes
                assertEquals(
                    "".toByteArray().contentToString(),
                    receivedPayload.contentToString(),
                    "Received payload on request topic did not match expected request payload"
                )
                // Publish a response message on the response topic
                mqttClient.publishWith()
                    .topic(publish.responseTopic.get())
                    .payload("\"Acknowledged\"".toByteArray())
                    .send()
            }
            .send().await()
        // Publish the message using invokeResource
        val response = client.invokeResource(form)
        assertEquals("application/json", response.type)
        assertEquals("\"Acknowledged\"", response.body.decodeToString())

        mqttClient.unsubscribeWith().topicFilter("thingId/actions/actionName").send().await()
    }

    @Test
    fun `invokeResource with content should publish given content to broker`() = runTest {
        val form = Form("$brokerUrl/thingId/actions/actionName", "application/json")
        val testMessage = "\"Hello World\""
        val expectedPayload = testMessage.toByteArray()
        val responseMessage = "\"Acknowledged\""
        val responsePayload = responseMessage.toByteArray()

        // Subscribe to the topic before publishing
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

        // Publish the message using invokeResource
        val response = client.invokeResource(form, Content("application/json", expectedPayload))
        assertEquals("application/json", response.type)
        assertEquals("\"Acknowledged\"", response.body.decodeToString())

        mqttClient.unsubscribeWith().topicFilter("thingId/actions/actionName").send().await()
    }

    @Test
    fun `subscribeResource should subscribe to broker and emit content via Flow`() = runTest {
        val form = Form("$brokerUrl/thingId/events/eventName", "application/json")

        client.subscribeResource(form).test {
            mqttClient.publish(Mqtt5Publish.builder()
                .topic("thingId/events/eventName")
                .payload("Hello World".toByteArray())
                .build()).await()
            val item = awaitItem()
            assertEquals("Hello World", item.body.decodeToString())

            client.unlinkResource(form)

            awaitComplete()
        }
    }

    @Test
    fun `writeResource should send content to broker and receive acknowledgment`() = runTest {
        val form = Form("$brokerUrl/thingId/properties/propertyName", "application/json")
        val content = Content("application/json", "\"New resource data\"".toByteArray())

        // Subscribe to the topic where the resource is expected to be written
        mqttClient.subscribeWith()
            .topicFilter("thingId/properties/propertyName")
            .callback { publish ->
                println("Received message on topic: ${publish.topic}")
                val receivedPayload = publish.payloadAsBytes
                // Check if the received payload matches the expected payload
                assertEquals("\"New resource data\"".toByteArray().contentToString(), receivedPayload.contentToString(), "Received payload on write topic did not match expected payload")

                // Publish a response message on the response topic
                mqttClient.publishWith()
                    .topic(publish.responseTopic.get())
                    .payload("\"Acknowledged\"".toByteArray())
                    .send()
            }
            .send().await()

        // Call writeResource to simulate the write operation
        client.writeResource(form, content)

        // Unsubscribe from the topic after the test
        mqttClient.unsubscribeWith().topicFilter("thingId/properties/propertyName").send().await()
    }

    @Test
    fun `readResource should receive content from broker`() = runTest {
        val form = Form("$brokerUrl/thingId/properties/propertyName", "application/json")

        // Subscribe to the topic where the resource is expected to be published
        mqttClient.subscribeWith()
            .topicFilter("thingId/properties/propertyName")
            .callback { publish ->
                println("Received message on topic: ${publish.topic}")
                val receivedPayload = publish.payloadAsBytes
                // Check if the received payload matches the expected payload
                assertEquals("".toByteArray().contentToString(), receivedPayload.contentToString(), "Received payload on read topic did not match expected payload")
                // Publish a response message on the response topic
                mqttClient.publishWith()
                    .topic(publish.responseTopic.get())
                    .payload("\"resource data\"".toByteArray())
                    .send()
            }
            .send().await()

        // Call readResource to simulate the read operation
        val response = client.readResource(form)

        // Check that the response matches the expected content
        assertEquals("application/json", response.type)
        assertEquals("\"resource data\"", response.body.decodeToString())

        // Unsubscribe from the topic after the test
        mqttClient.unsubscribeWith().topicFilter("thingId/properties/propertyName").send().await()
    }
}