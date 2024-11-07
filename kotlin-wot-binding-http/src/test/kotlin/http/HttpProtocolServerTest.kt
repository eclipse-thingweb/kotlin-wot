package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThingImpl
import ai.ancf.lmos.wot.thing.exposedThing
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.form.Operation.READ_PROPERTY
import ai.ancf.lmos.wot.thing.form.Operation.WRITE_PROPERTY
import ai.ancf.lmos.wot.thing.schema.PropertyReadHandler
import ai.ancf.lmos.wot.thing.schema.StringSchema
import ai.ancf.lmos.wot.thing.schema.stringSchema
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

private const val ACTION_NAME = "action1"

private const val EVENT_NAME = "event1"

private const val CONTENT_TYPE = "application/json"

class HttpProtocolServerTest {

    private lateinit var server: HttpProtocolServer
    private val servient: Servient = mockk()
    private val mockServer: EmbeddedServer<*, *> = mockk()
    private val exposedThing: ExposedThingImpl = exposedThing(servient) {
        intProperty(PROPERTY_NAME) {
            observable = true
            readHandler = PropertyReadHandler { 2 }
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

        val things : List<ExposedThingImpl> = response.body()
        assertEquals(1, things.size)


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

        val thing : ExposedThingImpl = response.body()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(exposedThing.id, thing.id)
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
        val propertyName = PROPERTY_NAME
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.get("/test/properties/$propertyName") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(2, response.body<Int>())

    }

    @Test
    fun `PUT on properties updates a thing property`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup test data for property
        val propertyName = PROPERTY_NAME
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.put("/test/properties/$propertyName") {
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
        val response = client.post("/test/actions/action1") {
            contentType(ContentType.Application.Json)
            setBody(""""input"""") // JSON payload for action input"
        }

        assertEquals(HttpStatusCode.OK, response.status)

        assertEquals("input", response.body<String>())
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