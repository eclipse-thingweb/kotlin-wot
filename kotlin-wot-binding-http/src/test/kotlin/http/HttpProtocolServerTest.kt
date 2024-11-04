package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.thing
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

class HttpProtocolServerTest {

    private lateinit var server: HttpProtocolServer
    private val servient: Servient = mockk()
    private val mockServer: EmbeddedServer<*, *> = mockk()
    private val exposedThing: ExposedThing = ExposedThing(
        thing("test") {
            intProperty("property1"){
                title = "title"
            }
            action("action1"){

            }
    })

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

        val things : List<ExposedThing> = response.body()

        assertEquals(1, things.size)

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(things, exposedThing)
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

        val thing : ExposedThing = response.body()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(thing, exposedThing)
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
    fun `PUT on properties updates a thing property`() = testApplication {
        application {
            setupRouting(servient)
        }
        val client = httpClient()
        // Setup test data for property
        val propertyName = "property1"
        every { servient.things } returns mutableMapOf(exposedThing.id to exposedThing)

        // Perform PUT request on property endpoint
        val response = client.put("/test/properties/$propertyName") {
            contentType(ContentType.Application.Json)
            setBody("""{ "value": "newValue" }""") // JSON payload to update the property
        }

        assertEquals(HttpStatusCode.OK, response.status)

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
            setBody("""{ "input": "someValue" }""") // JSON payload for action input
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }
}