package websocket

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolServer
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.schema.*
import io.mockk.clearAllMocks
import kotlinx.coroutines.test.runTest
import kotlin.test.*

private const val PROPERTY_NAME = "property1"

private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "action1"

private const val ACTION_NAME_2 = "action2"

private const val ACTION_NAME_3 = "action3"

private const val ACTION_NAME_4 = "action4"

private const val EVENT_NAME = "event1"

class WebSocketProtocolClientTest {

    private lateinit var thing: WoTConsumedThing

    private lateinit var servient : Servient

    private var property1 : Int = 0
    private var property2 : String = ""

    @BeforeTest
    fun setUp() = runTest {

        servient = Servient(
            servers = listOf(WebSocketProtocolServer()),
            clientFactories = listOf(HttpProtocolClientFactory(), WebSocketProtocolClientFactory())
        )

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
            property1.toInteractionInputValue()
        }.setPropertyReadHandler(PROPERTY_NAME_2) {
            5.toInteractionInputValue()
        }.setActionHandler(ACTION_NAME) { input, _->
            val inputString = input.value() as DataSchemaValue.StringValue
            "${inputString.value} 10".toInteractionInputValue()
        }.setPropertyWriteHandler(PROPERTY_NAME) { input, _->
            val inputInt = input.value() as DataSchemaValue.IntegerValue
            property1 = inputInt.value
            property1.toInteractionInputValue()
        }.setActionHandler(ACTION_NAME_2) { input, _->
            "test test".toInteractionInputValue()
        }.setActionHandler(ACTION_NAME_3) { input, _->
            val inputString = input.value() as DataSchemaValue.StringValue
            property2 = inputString.value
            InteractionInput.Value(DataSchemaValue.NullValue)
        }.setActionHandler(ACTION_NAME_4) { _, _->
            InteractionInput.Value(DataSchemaValue.NullValue)
        }.setEventSubscribeHandler(EVENT_NAME) { _ ->
        }

        property1 = 10

        servient.addThing(exposedThing)
        servient.start()
        servient.expose("test")

        val wot = Wot.create(servient)

        val thingDescription = wot.requestThingDescription("http://localhost:8080/test")
        thing = wot.consume(thingDescription)

    }

    @AfterTest
    fun tearDown() = runTest {
        clearAllMocks()
        servient.shutdown()
    }

    @Test
    fun `should get property`() = runTest{

        val readProperty1 = thing.readProperty(PROPERTY_NAME).value()
        assertEquals(10, (readProperty1 as DataSchemaValue.IntegerValue).value)

        val readProperty2 = thing.readProperty(PROPERTY_NAME_2).value()
        assertEquals(5, (readProperty2 as DataSchemaValue.IntegerValue).value)

    }

    @Test
    fun `should write property`() = runTest{
        thing.writeProperty(PROPERTY_NAME, 20.toInteractionInputValue())

        assertEquals(20, property1)
    }


    @Test
    fun `should invoke action`() = runTest{
        val response = thing.invokeAction(ACTION_NAME, "test".toInteractionInputValue()).value()

        assertEquals("test 10", (response as DataSchemaValue.StringValue).value)
    }

    @Test
    fun `should invoke action without input`() = runTest{
        val response = thing.invokeAction(ACTION_NAME_2).value()

        assertEquals("test test", (response as DataSchemaValue.StringValue).value)
    }

    @Test
    fun `should invoke action without output`() = runTest{
        val response = thing.invokeAction(ACTION_NAME_3, "test".toInteractionInputValue()).value()
        assertEquals("test", property2)
        assertIs<DataSchemaValue.NullValue>(response)
    }
}