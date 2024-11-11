package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.ThingDescription
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.form.Operation.READ_PROPERTY
import ai.ancf.lmos.wot.thing.form.Operation.WRITE_PROPERTY
import ai.ancf.lmos.wot.thing.schema.*
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.IntegerValue
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.StringValue
import ai.anfc.lmos.wot.binding.ProtocolServerException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

private const val PROPERTY_NAME = "property1"

private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "action1"

private const val ACTION_NAME_2 = "action2"

private const val ACTION_NAME_3 = "action3"

private const val ACTION_NAME_4 = "action4"

private const val EVENT_NAME = "event1"

private const val CONTENT_TYPE = "application/json"

class HttpProtocolServerTest {

    private lateinit var server: HttpProtocolServer
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
        server = HttpProtocolServer(createServer = { host, port, servient ->
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
    fun `expose should add a thing to things map`() = runTest {
        every { mockServer.start(any()) } answers { mockServer }

        // Expose the thing
        server.start(servient)  // Ensure server has started
        server.expose(exposedThing)

        assertTrue(server.things.containsKey(exposedThing.id))
    }

    @Test
    fun `expose should throw exception if server not started`(): Unit = runTest {
        // Assert
        assertFailsWith<ProtocolServerException> {
            server.expose(exposedThing)
        }
    }

    @Test
    fun `destroy should remove a thing from things map`() = runTest {
        every { mockServer.start(any()) } answers { mockServer }

        // Add the thing first
        server.start(servient)  // Ensure server has started
        server.expose(exposedThing)
        assertTrue(server.things.containsKey(exposedThing.id))

        // Now remove it
        server.destroy(exposedThing)
        assertFalse(server.things.containsKey(exposedThing.id))
    }

    @Test
    fun `GET on root route returns all things`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup test data
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform GET request on "/"
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)

        val thingDescriptions = response.body<List<ThingDescription>>()
        assertEquals(1, thingDescriptions.size)


    }

    @Test
    fun `GET on {id} route returns thing details`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup mock thing data
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform GET request on "/test"
        val response = client.get("/${exposedThing.id}")

        val thingDescription : ThingDescription = response.body()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(exposedThing.id, thingDescription.id)
    }

    @Test
    fun `GET on {id} returns 404 if thing not found`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Mock an empty things map
        every { servient.things } returns mutableMapOf()

        // Perform GET request on a non-existing thing ID
        val response = client.get("/nonexistentThing")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET on properties retrieves a thing property`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.get("/test/properties/$PROPERTY_NAME") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(10, response.body<Int>())

    }

    @Test
    fun `GET all properties`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.get("/test/properties") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseMap = response.body<Map<*, *>>()

        assertEquals(2, responseMap.entries.size)

        assertEquals(10, responseMap[PROPERTY_NAME])
        assertEquals(5, responseMap[PROPERTY_NAME_2])
    }

    @Test
    fun `PUT on properties updates a thing property`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup test data for property
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.put("/test/properties/$PROPERTY_NAME") {
            contentType(ContentType.Application.Json)
            setBody(2) // JSON payload to update the property
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, response.body<Int>())
    }

    private fun ApplicationTestBuilder.httpClient(): HttpClient {
        val client = createClient {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
        }
        return client
    }

    @Test
    fun `POST on actions invokes action`() = testApplication {
        application {
            setupRouting(servient)
        }
        // Setup action data
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform POST request on action endpoint
        val response = client.post("/test/actions/$ACTION_NAME") {
            contentType(ContentType.Application.Json)
            setBody("\"input\"") // JSON payload for action input
        }

        assertEquals(HttpStatusCode.OK, response.status)

        println( response.body<String>())

        assertEquals("\"input 10\"", response.body<String>())
    }

    @Test
    fun `POST without content on actions invokes action`() = testApplication {
        application {
            setupRouting(servient)
        }
        // Setup action data
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform POST request on action endpoint
        val response = client.post("/test/actions/$ACTION_NAME_2") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)

        println(response.body<String>())

        assertEquals("\"10\"", response.body<String>())
    }

    @Test
    fun `POST with only input invokes action`() = testApplication {
        application {
            setupRouting(servient)
        }
        // Setup action data
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform POST request on action endpoint
        val response = client.post("/test/actions/$ACTION_NAME_3") {
            contentType(ContentType.Application.Json)
            setBody("\"input\"") // JSON payload for action input
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST with no input and output invokes action`() = testApplication {
        application {
            setupRouting(servient)
        }
        // Setup action data
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform POST request on action endpoint
        val response = client.post("/test/actions/$ACTION_NAME_4") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `expose should throw exception if server is not started`() {
        // Arrange
        server.started = false // Set the server to not started

        // Act & Assert
        val exception = assertFailsWith<ProtocolServerException> {
            server.expose(exposedThing)
        }
        assertEquals("Server has not started yet", exception.message)
    }

    @Test
    fun `expose should expose thing and add forms`() {
        // Arrange
        server.started = true
        // Act
        server.expose(exposedThing)

        // Assert
        assertTrue(exposedThing.forms.isNotEmpty(), "Expected forms to be added to thing")

        val expectedHref = "http://0.0.0.0:8080/${exposedThing.id}/all/properties"
        val form = exposedThing.forms.find { it.href == expectedHref  }
        assertNotNull(form, "Expected form for reading all properties to be added")
        assertEquals(CONTENT_TYPE, form.contentType)
        assertEquals(2, form.op?.size)
    }

    @Test
    fun `exposeProperties should add forms for read write properties`() {
        // Arrange
        server.started = true
        val address = "http://0.0.0.0:8080"
        // Act
        server.exposeProperties(exposedThing, address, CONTENT_TYPE)

        // Assert
        val expectedHref = "$address/${exposedThing.id}/properties/$PROPERTY_NAME"
        val form = exposedThing.properties[PROPERTY_NAME]?.forms?.find { it.href == expectedHref }
        assertNotNull(form, "Expected form for property '$PROPERTY_NAME' to be added")

        assertTrue(form.op?.contains(READ_PROPERTY) == true, "Expected READ_PROPERTY operation")
        assertTrue(form.op?.contains(WRITE_PROPERTY) == true, "Expected WRITE_PROPERTY operation")
    }
    @Test
    fun `exposeActions should add form for action`() {
        // Arrange
        server.started = true
        val address = "http://0.0.0.0:8080"
        // Act
        server.exposeActions(exposedThing, address, CONTENT_TYPE)

        // Assert
        val expectedHref = "$address/${exposedThing.id}/actions/$ACTION_NAME"
        val form = exposedThing.actions[ACTION_NAME]?.forms?.find { it.href == expectedHref }
        assertNotNull(form, "Expected form for action 'action1' to be added")
        assertEquals(CONTENT_TYPE, form.contentType, "Content type should match")
        assertTrue(form.op?.contains(Operation.INVOKE_ACTION) == true, "Expected INVOKE_ACTION operation")
    }


    @Test
    fun `exposeEvents should add form for event`() {
        // Arrange
        server.started = true
        val address = "http://0.0.0.0:8080"
        // Act
        server.exposeEvents(exposedThing, address, CONTENT_TYPE)

        // Assert
        val expectedHref = "$address/${exposedThing.id}/events/$EVENT_NAME"
        val form = exposedThing.events[EVENT_NAME]?.forms?.find { it.href == expectedHref }
        assertNotNull(form, "Expected form for action 'action1' to be added")
        assertEquals(CONTENT_TYPE, form.contentType, "Content type should match")
        assertTrue(form.op?.contains(Operation.SUBSCRIBE_EVENT) == true, "Expected SUBSCRIBE_EVENT operation")

    }

}