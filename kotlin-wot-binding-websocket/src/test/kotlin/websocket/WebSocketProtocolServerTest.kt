package websocket

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.binding.websocket.PropertyReadingMessage
import ai.ancf.lmos.wot.binding.websocket.ReadPropertyMessage
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolServer
import ai.ancf.lmos.wot.binding.websocket.setupRoutingWithWebSockets
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.schema.*
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.IntegerValue
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.StringValue
import ai.anfc.lmos.wot.binding.ProtocolServerException
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import wiremock.org.apache.commons.lang3.concurrent.AbstractCircuitBreaker.PROPERTY_NAME
import kotlin.test.*

private const val PROPERTY_NAME = "property1"

private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "action1"

private const val ACTION_NAME_2 = "action2"

private const val ACTION_NAME_3 = "action3"

private const val ACTION_NAME_4 = "action4"

private const val EVENT_NAME = "event1"

private const val CONTENT_TYPE = "application/json"

class WebSocketProtocolServerTest {

    private lateinit var server: WebSocketProtocolServer
    private val servient: Servient = mockk()
    private val mockServer: EmbeddedServer<*, *> = mockk()
    private val exposedThing: ExposedThing = exposedThing(servient, id="test") {
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
        val inputString = input.value() as StringValue
        "${inputString.value} 10".toInteractionInputValue()
    }.setPropertyWriteHandler(PROPERTY_NAME) { input, _->
        val inputInt = input.value() as IntegerValue
        inputInt.value.toInteractionInputValue()
    }.setActionHandler(ACTION_NAME_2) { input, _->
        "10".toInteractionInputValue()
    }.setActionHandler(ACTION_NAME_3) { input, _->
        InteractionInput.Value(DataSchemaValue.NullValue)
    }.setActionHandler(ACTION_NAME_4) { _, _->
        InteractionInput.Value(DataSchemaValue.NullValue)
    }

    @BeforeTest
    fun setUp() {
        server = WebSocketProtocolServer(createServer = { host, port, servient ->
            mockServer
        })
    }

    @AfterTest
    fun tearDown() = runTest {
        clearAllMocks()
    }

    @Test
    fun `start should start the embedded server`() = runTest {
        every { mockServer.start(any()) } answers { mockServer }
        server.start(servient)

        verify { mockServer.start(any()) }
        assertTrue(server.started)
    }

    @Test
    fun `stop should throw exception if server not started`(): Unit = runTest {
        // Assert
        assertFailsWith<ProtocolServerException> {
            server.stop()
        }
    }

    @Test
    fun `stop should stop the embedded server`() = runTest {
        every { mockServer.start(any()) } answers { mockServer }
        every { mockServer.stop(any(), any()) } just Runs

        server.start(servient)

        // Stop the server
        server.stop()
        verify { mockServer.stop(any(), any()) }
        assertFalse(server.started)
    }

    @Test
    fun `GET on property on ReadPropertyMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter()
            }
        }
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(ReadPropertyMessage("test", property = PROPERTY_NAME))

            val readingMessage = receiveDeserialized<PropertyReadingMessage>()

            println("readingMessage: $readingMessage")
        }

        // Close the client and clean up
        client.close()

    }

}