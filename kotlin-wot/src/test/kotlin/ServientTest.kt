package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentCodecException
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.Context
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import ai.anfc.lmos.wot.binding.ProtocolServer
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.reflect.KClass
import kotlin.test.*

class ServientTest {

    private val mockServer1 = mockk<ProtocolServer>(relaxed = true)
    private val mockServer2 = mockk<ProtocolServer>(relaxed = true)
    private val mockClient = mockk<ProtocolClient>()
    private val factoryMock = mockk<ProtocolClientFactory>{
        every { scheme } returns "http"
        every { client } returns mockClient
        coEvery { init() } just Runs
        coEvery { destroy() } just Runs
    }
    private val mockThing = mockk<ExposedThing>() {
        every { id } returns "testThing"
    }
    private val mockContent = mockk<Content>()

    private val servient = Servient(
        servers = listOf(mockServer1, mockServer2),
        clientFactories = listOf(factoryMock),
        things = mutableMapOf("testThing" to mockThing)
    )

    @BeforeTest
    fun setUp() {
        mockkObject(ContentManager)
    }

    @AfterTest
    fun tearDown() {
        unmockkObject(ContentManager)
        unmockkAll()
    }


    @Test
    fun `start - should start all servers and clients`() = runTest {
        // Act
        servient.start()

        // Assert
        coVerify { mockServer1.start(servient) }
        coVerify { mockServer2.start(servient) }
        coVerify { factoryMock.init() }
    }

    @Test
    fun `shutdown - should stop all servers and clients`() = runTest {
        // Act
        servient.shutdown()

        // Assert
        coVerify { mockServer1.stop() }
        coVerify { mockServer2.stop() }
        coVerify { factoryMock.destroy() }
    }

    @Test
    fun `expose - should throw exception if thing is not added`(): Unit = runTest {
        val invalidId = "invalidThing"

        // Assert
        assertFailsWith<ServientException> {
            servient.expose(invalidId)
        }
    }

    @Test
    fun `expose - should expose the thing on all servers`() = runTest {
        // Act
        servient.expose("testThing")

        // Assert
        coVerify { mockServer1.expose(mockThing) }
        coVerify { mockServer2.expose(mockThing) }
    }

    @Test
    fun `destroy - should throw exception if thing is not added`(): Unit = runTest {
        val invalidId = "invalidThing"

        // Assert
        assertFailsWith<ServientException> {
            servient.destroy(invalidId)
        }
    }

    @Test
    fun `destroy - should stop exposing the thing on all servers`() = runTest {
        // Act
        servient.destroy("testThing")

        // Assert
        coVerify { mockServer1.destroy(mockThing) }
        coVerify { mockServer2.destroy(mockThing) }
    }

    @Test
    fun `addThing - should return true if thing is added successfully`() {
        // Arrange
        val newThing = mockk<ExposedThing> {
            every { id } returns "newThing"
        }

        // Act
        val result = servient.addThing(newThing)

        // Assert
        assertTrue(result)
        assertEquals(newThing, servient.things["newThing"])
    }

    @Test
    fun `addThing - should return false if thing already exists`() {
        // Act
        val result = servient.addThing(mockThing)

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `fetch should return Thing when successful`() = runTest {
        // Arrange
        val url = URI("http://example.com")
        val thingAsJsonString = """
            {
              "id": "Foo",
              "description": "Bar",
              "@type": "Thing",
              "@context": ["http://www.w3.org/ns/td"]
            }
            """

        // Mock readResource to return mockContent
        coEvery { mockClient.readResource(any<Form>()) } returns
                Content(ContentManager.DEFAULT_MEDIA_TYPE, thingAsJsonString.toByteArray())

        // Act
        val fetchedThing = servient.fetch(url)

        // Assert
        assertEquals("Foo", fetchedThing.id)
        assertEquals(Type("Thing"), fetchedThing.objectType)
        assertEquals(Context("http://www.w3.org/ns/td"), fetchedThing.objectContext)
    }

    @Test
    fun `fetch should throw ServientException when ProtocolClientException occurs`() = runTest {
        // Arrange
        val url = URI("http://example.com")
        val scheme = url.scheme

        coEvery { mockClient.readResource(any<Form>()) } throws ProtocolClientException("Client error")

        // Act & Assert
        val exception = assertThrows<ServientException> {
            runBlocking { servient.fetch(url) }
        }
        assertEquals("Unable to fetch thing description: Client error", exception.message)
        verify { servient.getClientFor(scheme) }
        coVerify { mockClient.readResource(any<Form>()) }
    }

    @Test
    fun `fetch should throw ServientException when ContentCodecException occurs`() = runTest {
        // Arrange
        val url = URI("http://example.com")
        coEvery { mockClient.readResource(any<Form>()) } returns mockContent

        every { ContentManager.contentToValue(mockContent, any<KClass<*>>()) } throws ContentCodecException("Codec error")

        // Act & Assert
        val exception = assertThrows<ServientException> {
            runBlocking { servient.fetch(url) }
        }
        assertEquals("Error while fetching thing description: Codec error", exception.message)
        coVerify { mockClient.readResource(any<Form>()) }
        verify { ContentManager.contentToValue(mockContent, any<KClass<*>>()) }
    }

    /*

    @Test
    fun `test fetchDirectory success`() = runBlocking {
        // Arrange
        val expectedThings = listOf(ExposedThingImpl(servient, id = "test"))

        // Mocking ProtocolClient's readResource method
        coEvery { mockClient.readResource(any<Resource>()) } returns mockContent

        // Mocking ContentManager to simulate content to value conversion
        mockkObject(ContentManager)
        every { ContentManager.contentToValue(mockContent, ArraySchema<Map<*,*>>()) } returns JsonNode.ArrayValue(expectedThings)

        // Act
        val result = servient.fetchDirectory("http://example.com")

        // Assert
        assertEquals(1, result.size)
    }

    */
}