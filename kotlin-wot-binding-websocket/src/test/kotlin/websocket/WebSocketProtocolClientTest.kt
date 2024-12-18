package websocket

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolServer
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.schema.*
import io.mockk.clearAllMocks
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PROPERTY_NAME = "property1"

private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "action1"

private const val ACTION_NAME_2 = "action2"

private const val ACTION_NAME_3 = "action3"

private const val ACTION_NAME_4 = "action4"

private const val EVENT_NAME = "event1"

class WebSocketProtocolClientTest {

    private lateinit var servient: Servient
    private lateinit var wot: Wot
    private var server = WebSocketProtocolServer()

    @BeforeTest
    fun setUp() = runTest {

        servient = Servient(
            servers = listOf(server),
            clientFactories = listOf(WebSocketProtocolClientFactory()))

        val exposedThing = exposedThing(servient, id="test") {
            intProperty(PROPERTY_NAME) {
                observable = true
            }
            stringProperty(PROPERTY_NAME_2) {
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
            event<String, Nothing, Nothing>(EVENT_NAME){
                data = StringSchema()
            }
        }.setPropertyReadHandler(PROPERTY_NAME) {
            10.toInteractionInputValue()
        }.setPropertyReadHandler(PROPERTY_NAME_2) {
            5.toInteractionInputValue()
        }.setActionHandler(ACTION_NAME) { input, _->
            val inputString = input.value() as DataSchemaValue.StringValue
            "${inputString.value} 10".toInteractionInputValue()
        }.setPropertyWriteHandler(PROPERTY_NAME) { input, _->
            val inputInt = input.value() as DataSchemaValue.IntegerValue
            inputInt.value.toInteractionInputValue()
        }.setActionHandler(ACTION_NAME_2) { input, _->
            "10".toInteractionInputValue()
        }.setActionHandler(ACTION_NAME_3) { input, _->
            InteractionInput.Value(DataSchemaValue.NullValue)
        }.setActionHandler(ACTION_NAME_4) { _, _->
            InteractionInput.Value(DataSchemaValue.NullValue)
        }.setEventSubscribeHandler(EVENT_NAME) { _ ->
        }

        servient.addThing(exposedThing)
        servient.start()
        servient.expose("test")

        wot = Wot.create(servient)

    }

    @AfterTest
    fun tearDown() = runTest {
        clearAllMocks()
    }

    @Test
    fun `should get property`() = runTest{

        val exposedThing = server.things["test"]!!

        val thing = wot.consume(exposedThing.getThingDescription())
        assertEquals("test", thing.getThingDescription().id)
        val readProperty = thing.readProperty(PROPERTY_NAME).value()

    }
}