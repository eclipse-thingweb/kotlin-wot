/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package websocket

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.binding.websocket.*
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.schema.InteractionInput
import ai.ancf.lmos.wot.thing.schema.StringSchema
import ai.ancf.lmos.wot.thing.schema.stringSchema
import ai.ancf.lmos.wot.thing.schema.toInteractionInputValue
import ai.anfc.lmos.wot.binding.ProtocolServerException
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertInstanceOf
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
        val inputString = input.value()
        "${inputString.asText()} 10".toInteractionInputValue()
    }.setPropertyWriteHandler(PROPERTY_NAME) { input, _->
        val inputInt = input.value()
        inputInt.asInt().toInteractionInputValue()
    }.setActionHandler(ACTION_NAME_2) { input, _->
        "10".toInteractionInputValue()
    }.setActionHandler(ACTION_NAME_3) { input, _->
        InteractionInput.Value(NullNode.instance)
    }.setActionHandler(ACTION_NAME_4) { _, _->
        InteractionInput.Value(NullNode.instance)
    }.setEventSubscribeHandler(EVENT_NAME) { _ ->
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
    fun `Get property on ReadPropertyMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(ReadPropertyMessage("test", property = PROPERTY_NAME))

            val readingMessage = receiveDeserialized<PropertyReadingMessage>()

            assertEquals(10, readingMessage.data.asInt())
        }
    }

    @Test
    fun `Get properties on ReadAllPropertiesMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(ReadAllPropertiesMessage("test"))

            val readingMessage = receiveDeserialized<PropertyReadingsMessage>()

            assertEquals(2, readingMessage.data.size)
        }
    }

    @Test
    fun `Write property on WritePropertyMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(WritePropertyMessage("test", property = PROPERTY_NAME, data = IntNode(10)))

            val readingMessage = receiveDeserialized<PropertyReadingMessage>()

            assertEquals(10, readingMessage.data.asInt())
        }
    }

    @Test
    fun `Invoke action on InvokeActionMessage with input`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(InvokeActionMessage("test", action = ACTION_NAME, input = TextNode("test")))

            val actionStatus = receiveDeserialized<ActionStatusMessage>()

            assertEquals(ActionStatus.COMPLETED, actionStatus.status)
            assertEquals("test 10", actionStatus.output?.asText())
        }
    }

    @Test
    fun `Invoke action on InvokeActionMessage with input and no output`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(InvokeActionMessage("test", action = ACTION_NAME_3, input = TextNode("test")))

            val actionStatus = receiveDeserialized<ActionStatusMessage>()

            assertEquals(ActionStatus.COMPLETED, actionStatus.status)
            assertInstanceOf<NullNode>(actionStatus.output)
        }
    }

    @Test
    fun `Invoke action on InvokeActionMessage no input and no output`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(InvokeActionMessage("test", action = ACTION_NAME_4))

            val actionStatus = receiveDeserialized<ActionStatusMessage>()

            assertEquals(ActionStatus.COMPLETED, actionStatus.status)
            assertInstanceOf<NullNode>(actionStatus.output)
        }
    }

    @Test
    fun `Invoke action on InvokeActionMessage without input`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }

        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }

        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(InvokeActionMessage("test", action = ACTION_NAME_2))

            val actionStatus = receiveDeserialized<ActionStatusMessage>()

            assertEquals(ActionStatus.COMPLETED, actionStatus.status)
            assertEquals("10", actionStatus.output?.asText())
        }
    }

    @Test
    fun `Observe property on ObservePropertyMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }
        every { mockServer.start(any()) } answers { mockServer }
        server.start(servient)
        server.expose(exposedThing)
        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {
            sendSerialized(ObservePropertyMessage("test", property = PROPERTY_NAME))

            var readingMessage = receiveDeserialized<PropertyReadingMessage>()

            assertEquals(10, readingMessage.data.asInt())

            exposedThing.emitPropertyChange(PROPERTY_NAME, 5.toInteractionInputValue())

            readingMessage = receiveDeserialized<PropertyReadingMessage>()

            assertEquals(5, readingMessage.data.asInt())
        }
    }

    @Test
    fun `Subscribe event on SubscribeEventMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }
        every { mockServer.start(any()) } answers { mockServer }
        server.start(servient)
        server.expose(exposedThing)
        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {

            // Perform the sendSerialized operation
            sendSerialized(SubscribeEventMessage("test", event = EVENT_NAME))
            // Signal that the sending is complete
            val acknowledgement = receiveDeserialized<Acknowledgement>()

            assertEquals(MessageTypes.SUBSCRIBE_EVENT, acknowledgement.message)

            exposedThing.emitEvent(EVENT_NAME, "\"testEvent\"".toInteractionInputValue())

            val eventMessage = receiveDeserialized<EventMessage>()

            assertEquals("\"testEvent\"", eventMessage.data.asText())

        }
    }

    @Test
    fun `Unsubscribe event on UnsubscribeEventMessage`() = testApplication {
        application {
            setupRoutingWithWebSockets(servient)
        }
        every { mockServer.start(any()) } answers { mockServer }
        server.start(servient)
        server.expose(exposedThing)
        val client = createClient {
            install(WebSockets){
                contentConverter = JacksonWebsocketContentConverter(JsonMapper.instance)
            }
        }
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        client.webSocket("/ws") {

            // Perform the sendSerialized operation
            sendSerialized(SubscribeEventMessage("test", event = EVENT_NAME))
            // Signal that the sending is complete
            val subscribeAcknowledgement = receiveDeserialized<Acknowledgement>()
            // Perform the sendSerialized operation
            sendSerialized(UnsubscribeEventMessage("test", event = EVENT_NAME))
            // Signal that the sending is complete
            val unsubscribeAcknowledgement = receiveDeserialized<Acknowledgement>()

            assertEquals(MessageTypes.UNSUBSCRIBE_EVENT, unsubscribeAcknowledgement.message)

        }
    }

}