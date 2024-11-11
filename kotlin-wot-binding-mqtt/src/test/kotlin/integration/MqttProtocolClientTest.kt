package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.form.Form
import app.cash.turbine.test
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MqttProtocolClientTest {

    private lateinit var mqttClientConfig: MqttClientConfig
    private lateinit var mqttClient: Mqtt5AsyncClient
    private lateinit var topicSubjects: MutableMap<String, Flow<Content>>
    private lateinit var client: MqttProtocolClient
    private lateinit var form: Form
    private lateinit var content: Content

    @BeforeTest
    fun setUp() {
        mqttClientConfig = mockk()
        mqttClient = mockk(relaxed = true)
        topicSubjects = mutableMapOf()
        form = mockk()
        content = mockk()

        every { mqttClientConfig.broker } returns "mqtt://test.mosquitto.org"
    }

    @Test
    fun `invokeResource should publish null to broker`() = runTest {
        every { form.href } returns "mqtt://test.mosquitto.org/counter/actions/increment"
        client = MqttProtocolClient(Pair(mqttClientConfig, mqttClient), topicSubjects)

        client.invokeResource(form)

        verify { mqttClient.publish(any<Mqtt5Publish>()) }
    }

    @Test
    fun `invokeResource with content should publish given content to broker`() = runTest {
        every { form.href } returns "mqtt://test.mosquitto.org/counter/actions/increment"
        every { content.body } returns "Hello World".toByteArray()

        client = MqttProtocolClient(Pair(mqttClientConfig, mqttClient), topicSubjects)
        client.invokeResource(form, content)

        verify { mqttClient.publish(any<Mqtt5Publish>()) }
    }

    @Test
    fun `subscribeResource should subscribe to broker and emit content via Flow`() = runTest {
        every { form.href } returns "mqtt://test.mosquitto.org/counter/events/change"
        client = MqttProtocolClient(Pair(mqttClientConfig, mqttClient), mutableMapOf())


        client.subscribeResource(form).test {
            val item = awaitItem()
            assertEquals("Hello World", item.body.decodeToString())
            awaitComplete()
        }

        verify { mqttClient.subscribeWith().topicFilter("counter/events/change").qos(any()).send() }
    }

    @Test
    fun `subscribeResource should reuse existing broker subscriptions`() = runTest {
        every { form.href } returns "mqtt://test.mosquitto.org/counter/events/change"

        val existingFlow = flowOf(Content("application/json", "Existing Data".toByteArray()))
        topicSubjects["counter/events/change"] = existingFlow

        client = MqttProtocolClient(Pair(mqttClientConfig, mqttClient), topicSubjects)

        client.subscribeResource(form).test {
            val item = awaitItem()
            assertEquals("Existing Data", item.body.decodeToString())
            awaitComplete()
        }
    }

    @Test
    fun `subscribeResource should unsubscribe from broker when no more subscriptions`() = runTest {
        every { form.href } returns "mqtt://test.mosquitto.org/counter/events/change"
        client = MqttProtocolClient(Pair(mqttClientConfig, mqttClient), mutableMapOf())

        client.subscribeResource(form).test {
            cancelAndIgnoreRemainingEvents()
        }

        verify { mqttClient.unsubscribeWith().topicFilter("counter/events/change").send() }
    }
}