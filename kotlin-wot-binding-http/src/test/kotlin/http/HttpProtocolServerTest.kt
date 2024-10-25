package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThing
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class HttpProtocolServerTest {

    private lateinit var server: HttpProtocolServer
    private val servient: Servient = mockk(relaxed = true)
    private val embeddedServer: ApplicationEngine = mockk(relaxed = true)
    private val exposedThing: ExposedThing = mockk(relaxed = true)

    @BeforeTest
    fun setUp() {
        server = HttpProtocolServer()
    }

    @AfterTest
    fun tearDown() = runBlocking {
        server.stop()
        clearAllMocks()
    }

    @Test
    fun `start should initialize and start the server`() = runBlocking {
        every { embeddedServer.start(any()) } just Awaits
        server.start(servient)

        verify { servient wasNot Called }
        assertTrue(server.started)
    }

    @Test
    fun `stop should shutdown the server`() = runBlocking {
        server = HttpProtocolServer() // reset server state

        // Start the server first
        server.start(servient)
        assertTrue(server.started)

        // Stop the server
        server.stop()
        assertFalse(server.started)
    }

    @Test
    fun `expose should add a thing to things map`() = runBlocking {
        every { exposedThing.id } returns "testThing"

        // Expose the thing
        server.start(servient)  // Ensure server has started
        server.expose(exposedThing)

        assertTrue(server.things.containsKey("testThing"))
        verify { exposedThing.id }
    }

    @Test
    fun `destroy should remove a thing from things map`() = runBlocking {
        every { exposedThing.id } returns "testThing"

        // Add the thing first
        server.start(servient)  // Ensure server has started
        server.expose(exposedThing)
        assertTrue(server.things.containsKey("testThing"))

        // Now remove it
        server.destroy(exposedThing)
        assertFalse(server.things.containsKey("testThing"))
    }

    @Test
    fun `GET on root route returns all things`() = testApplication {
        // Setup test data
        every { servient.things } returns mutableMapOf("thing1" to exposedThing)

        // Perform GET request on "/"
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        verify { servient.things } // Verify that the things were requested
    }

    @Test
    fun `GET on {id} route returns thing details`() = testApplication {
        // Setup mock thing data
        every { exposedThing.id } returns "testThing"
        every { servient.things } returns mutableMapOf("testThing" to exposedThing)

        // Perform GET request on "/testThing"
        val response = client.get("/testThing")

        assertEquals(HttpStatusCode.OK, response.status)
        verify { servient.things }
    }

    @Test
    fun `GET on {id} returns 404 if thing not found`() = testApplication {
        // Mock an empty things map
        every { servient.things } returns mutableMapOf()

        // Perform GET request on a non-existing thing ID
        val response = client.get("/nonexistentThing")

        assertEquals(HttpStatusCode.NotFound, response.status)
        verify { servient.things }
    }

    @Test
    fun `PUT on properties updates a thing property`() = testApplication {
        // Setup test data for property
        val propertyName = "property1"
        every { exposedThing.id } returns "testThing"
        every { servient.things } returns mutableMapOf("testThing" to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.put("/testThing/properties/$propertyName") {
            contentType(ContentType.Application.Json)
            setBody("""{ "value": "newValue" }""") // JSON payload to update the property
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST on actions invokes action`() = testApplication {
        // Setup action data
        every { exposedThing.id } returns "testThing"
        every { servient.things } returns mutableMapOf("testThing" to exposedThing)

        // Perform POST request on action endpoint
        val response = client.post("/testThing/actions/action1") {
            contentType(ContentType.Application.Json)
            setBody("""{ "input": "someValue" }""") // JSON payload for action input
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}