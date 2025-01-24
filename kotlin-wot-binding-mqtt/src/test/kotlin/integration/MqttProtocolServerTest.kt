package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import ai.ancf.lmos.wot.thing.schema.StringSchema
import ai.ancf.lmos.wot.thing.schema.stringSchema
import ai.ancf.lmos.wot.thing.schema.toInteractionInputValue
import app.cash.turbine.test
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds


class MqttProtocolServerTest {
    companion object {

        private lateinit var hiveMqContainer: GenericContainer<*>
        private lateinit var mqttClient: Mqtt5AsyncClient
        private lateinit var protocolClient: MqttProtocolClient
        private lateinit var mqttServer: MqttProtocolServer
        private lateinit var exposedThing: ExposedThing
        private lateinit var brokerUrl: String

        private const val PROPERTY_NAME = "property1"

        private const val ACTION_NAME = "action1"

        private const val EVENT_NAME = "event1"

        @BeforeAll
        @JvmStatic
        fun setUp() =  runTest {
            hiveMqContainer = GenericContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"))
                .withExposedPorts(1883)
            hiveMqContainer.start()

            val host = hiveMqContainer.host
            val mappedPort = hiveMqContainer.getMappedPort(1883)

            mqttClient = Mqtt5Client.builder()
                .serverHost(host)
                .serverPort(mappedPort)
                .buildAsync()
            mqttClient.connect()

            brokerUrl = "mqtt://$host:${mappedPort}"

            val clientConfig = MqttClientConfig(host, mappedPort, "client")
            val serverConfig = MqttClientConfig(host, mappedPort, "server")

            val clientFactory = MqttProtocolClientFactory(clientConfig)
            clientFactory.init()
            protocolClient = clientFactory.client

            val servient = Servient()
            mqttServer = MqttProtocolServer(serverConfig)
            mqttServer.start(servient)
            exposedThing = exposedThing(servient, id = "test") {
                stringProperty(PROPERTY_NAME) {
                    observable = true
                }
                action<String, String>(ACTION_NAME)
                {
                    title = ACTION_NAME
                    input = stringSchema {
                        title = "Action Input"
                        minLength = 10
                        default = "test"
                    }
                    output = StringSchema()
                }
                event<String, Nothing, Nothing>(EVENT_NAME){
                    data = StringSchema()
                }
            }.setPropertyReadHandler(PROPERTY_NAME) {
                "\"testOutput\"".toInteractionInputValue()
            }.setActionHandler(ACTION_NAME) { input, _ ->
                val inputString = input.value() as DataSchemaValue.StringValue
                "\"${inputString.value} 10\"".toInteractionInputValue()
            }.setPropertyWriteHandler(PROPERTY_NAME) { input, _ ->
                try {
                    val inputInt = input.value() as DataSchemaValue.StringValue
                    inputInt.value.toInteractionInputValue()
                } catch (e: Exception) {
                    throw IllegalArgumentException("Invalid input", e)
                }
            }

            mqttServer.expose(exposedThing)

        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            hiveMqContainer.stop()
        }
    }

    @Test
    fun `start should connect to MQTT broker`() = runTest {
        // broker is started in setup
        assertTrue(mqttClient.state.isConnected, "MQTT client should be connected")
    }

    @Test
    fun `expose should publish Thing Description and receive a response`(): Unit = runBlocking {
        val requestTopic = exposedThing.id
        val responseTopic = "${exposedThing.id}/td"
        val messagePayload = "TestMessage"

        val lock = CountDownLatch(1)
        var responsePayload: ByteArray? = null

        // Subscribe to the response topic
        mqttClient.subscribeWith()
            .topicFilter(responseTopic)
            .callback { publish ->
                responsePayload = publish.payloadAsBytes
                lock.countDown()
            }
            .send()
            .await()

        // Publish the message to the request topic
        mqttClient.publishWith()
            .topic(requestTopic)
            .payload(messagePayload.toByteArray())
            .responseTopic(responseTopic)
            .send()
            .await()

        // Wait for the response, with a timeout.
        val completedInTime = lock.await(2000, TimeUnit.MILLISECONDS)

        // Assert that the response was received in time and matches expectations
        assertTrue(completedInTime, "Expected response was not received within the timeout period.")
        assertNotNull(responsePayload, "Response payload is null.")

        //assertEquals("test", ThingDescription.fromBytes(responsePayload!!).id)
    }

    @Test
    fun `handle property read requests`() = runTest {
        // Test read request
        val content = protocolClient
            .readResource(Form("${brokerUrl}/${exposedThing.id}/properties/$PROPERTY_NAME",
                "application/json"))

        val readValue = JsonMapper.instance.readValue(content.body, String::class.java)
        assertEquals("\"testOutput\"", readValue)
    }

    @Test
    fun `handle property write requests`() = runTest {
        // Test write request
        protocolClient.writeResource(Form("${brokerUrl}/${exposedThing.id}/properties/$PROPERTY_NAME",
                "application/json"), Content("application/json", "\"testInput\"".toByteArray())
            )
    }


   @Test
   fun `handle action invocation requests`() = runTest {
       val content = protocolClient.invokeResource(
           Form(
               "${brokerUrl}/${exposedThing.id}/actions/$ACTION_NAME",
               "application/json"
           ), Content("application/json", "\"testInput\"".toByteArray())
       )

       val readValue = JsonMapper.instance.readValue(content.body, String::class.java)
       assertEquals("\"testInput 10\"", readValue)
   }


    @Test
    fun `subscribe to event and verify publishing`() = runTest {

        val events = protocolClient.subscribeResource(Form(
          "${brokerUrl}/${exposedThing.id}/events/$EVENT_NAME",
          "application/json"
        ))

        events.test(timeout = 5.seconds) {
          // Trigger event
          exposedThing.emitEvent(EVENT_NAME, "\"testEvent\"".toInteractionInputValue())

          // Verify that the event is emitted in the flow
          val content = awaitItem()
          val stringValue = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
          assertEquals("\"testEvent\"", stringValue.value)

          // Optionally verify no further emissions or complete the flow
          cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscribe to property change and verify publishing`() = runTest {

        val events = protocolClient.subscribeResource(Form(
            "${brokerUrl}/${exposedThing.id}/properties/$PROPERTY_NAME/observable",
            "application/json"
        ))

        events.test(timeout = 5.seconds) {
            // Trigger event
            exposedThing.emitPropertyChange(PROPERTY_NAME, "\"testPropertyChange\"".toInteractionInputValue())

            // Verify that the event is emitted in the flow
            val content = awaitItem()
            val stringValue = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
            assertEquals("\"testPropertyChange\"", stringValue.value)

            // Optionally verify no further emissions or complete the flow
            cancelAndIgnoreRemainingEvents()
        }
    }

}