package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolClientFactory
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolServer
import ai.ancf.lmos.wot.thing.schema.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PROPERTY_NAME = "property1"
private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "action1"

private const val ACTION_NAME_2 = "action2"

private const val ACTION_NAME_3 = "action3"

private const val ACTION_NAME_4 = "action4"

private const val EVENT_NAME = "event1"

class WoTMqttIntegrationTest() {

    companion object {

        private lateinit var hiveMqContainer: GenericContainer<*>

        private const val PROPERTY_NAME = "property1"

        private const val ACTION_NAME = "action1"

        private const val EVENT_NAME = "event1"


        @BeforeAll
        @JvmStatic
        fun setUp() =  runTest {
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

    @Test
    fun `Should fetch thing`() = runTest {
        val config = MqttClientConfig(hiveMqContainer.host,
            hiveMqContainer.getMappedPort(1883), "testClient")

        val servient = Servient(
            servers = listOf(MqttProtocolServer(config)),
            clientFactories = listOf(MqttProtocolClientFactory(config))
        )
        val wot = Wot.create(servient)

        val exposedThing = wot.produce {
            id = "myid"
            title = "MyThing"
            intProperty(PROPERTY_NAME) {
                observable = true
            }
            intProperty(PROPERTY_NAME_2) {
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
            action<String, String>(ACTION_NAME_2)
            {
                title = ACTION_NAME_2
                output = StringSchema()
            }
            action<String, String>(ACTION_NAME_3)
            {
                title = ACTION_NAME_3
                input = StringSchema()
            }
            action<String, String>(ACTION_NAME_4)
            {
                title = ACTION_NAME_4
            }
            event<String, Nothing, Nothing>(EVENT_NAME) {
                data = StringSchema()
            }
        }.setPropertyReadHandler(PROPERTY_NAME) {
            10.toInteractionInputValue()
        }.setPropertyReadHandler(PROPERTY_NAME_2) {
            5.toInteractionInputValue()
        }.setActionHandler(ACTION_NAME) { input, _ ->
            val inputString = input.value() as DataSchemaValue.StringValue
            "${inputString.value} 10".toInteractionInputValue()
        }.setPropertyWriteHandler(PROPERTY_NAME) { input, _ ->
            val inputInt = input.value() as DataSchemaValue.IntegerValue
            inputInt.value.toInteractionInputValue()
        }.setActionHandler(ACTION_NAME_2) { input, _ ->
            "10".toInteractionInputValue()
        }.setActionHandler(ACTION_NAME_3) { input, _ ->
            InteractionInput.Value(DataSchemaValue.NullValue)
        }.setActionHandler(ACTION_NAME_4) { _, _ ->
            InteractionInput.Value(DataSchemaValue.NullValue)
        }.setPropertyObserveHandler(PROPERTY_NAME) {
            10.toInteractionInputValue()
        }

        //exposedThing.setPropertyWriteHandler(PROPERTY_NAME) { input -> input }
        //exposedThing.setActionHandler(ACTION) { input, options -> "actionOutput: $input" }

        servient.start()
        servient.addThing(exposedThing)
        servient.expose("myid")

        //val fetchedThings = servient.fetchDirectory("http://localhost:8080")

        //assertEquals(1, fetchedThings.size)

        val brokerUrl = "mqtt://${hiveMqContainer.host}:${hiveMqContainer.getMappedPort(1883)}"
        val thingDescription = wot.requestThingDescription("${brokerUrl}/myid")

        val consumedThing = wot.consume(thingDescription)

        assertEquals(consumedThing.getThingDescription().id, exposedThing.id)

        val readProperty = consumedThing.readProperty(PROPERTY_NAME)

        val propertyResponse = readProperty.value() as DataSchemaValue.IntegerValue

        assertEquals(10, propertyResponse.value)

        consumedThing.writeProperty(PROPERTY_NAME, 20.toInteractionInputValue())

        val output = consumedThing.invokeAction(ACTION_NAME, "actionInput".toInteractionInputValue(), null)

        val actionResponse = output.value() as DataSchemaValue.StringValue

        assertEquals("actionInput 10", actionResponse.value)

        val responseMap = consumedThing.readAllProperties()

        assertEquals(2, responseMap.size)
        assertEquals(DataSchemaValue.IntegerValue(10), responseMap[PROPERTY_NAME]?.value())
        assertEquals(DataSchemaValue.IntegerValue(5), responseMap[PROPERTY_NAME_2]?.value())

        consumedThing.observeProperty(PROPERTY_NAME, listener = { println("Property observed: $it") })

        exposedThing.emitPropertyChange(PROPERTY_NAME, 30.toInteractionInputValue())
    }
}