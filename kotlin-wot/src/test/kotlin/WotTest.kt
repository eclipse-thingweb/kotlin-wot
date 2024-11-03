package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.filter.DiscoveryMethod
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import java.net.URI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class WotTest {

    // Mocked dependency
    private lateinit var servient: Servient
    private lateinit var defaultWot: DefaultWot

    @BeforeTest
    fun setup() {
        servient = mockk()  // Mock the Servient class
        defaultWot = DefaultWot(servient)
    }

    @Test
    fun `test discover with filter`() = runTest {
        // Given
        val filter = ThingFilter(method = DiscoveryMethod.ANY)
        val thing = mockk<Thing>()
        coEvery { servient.discover(filter) } returns flowOf(thing)

        // When
        val result = defaultWot.discover(filter).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(thing, result.first())
        coVerify { servient.discover(filter) }
    }

    @Test
    fun `test discover without filter`() = runTest {
        // Given
        val filter = ThingFilter(method = DiscoveryMethod.ANY)
        val thing = mockk<Thing>()
        coEvery { servient.discover(filter) } returns flowOf(thing)

        // When
        val result = defaultWot.discover().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(thing, result.first())
        coVerify { servient.discover(filter) }
    }

    @Test
    fun `test fetch with URI`() = runTest {
        // Given
        val url = URI("http://example.com")
        val thing = mockk<Thing>()
        coEvery { servient.fetch(url) } returns thing

        // When
        val result = defaultWot.fetch(url)

        // Then
        assertEquals(thing, result)
        coVerify { servient.fetch(url) }
    }

    @Test
    fun `test fetch with String URL`() = runTest {
        // Given
        val urlString = "http://example.com"
        val thing = mockk<Thing>()
        coEvery { servient.fetch(urlString) } returns thing

        // When
        val result = defaultWot.fetch(urlString)

        // Then
        assertEquals(thing, result)
        coVerify { servient.fetch(urlString) }
    }

    @Test
    fun `test destroy`() = runTest {
        // Given
        coEvery { servient.shutdown() } just runs

        // When
        defaultWot.destroy()

        // Then
        coVerify { servient.shutdown() }
    }


    @Test
    fun `produce should return ExposedThing when adding is successful`() = runTest {
        // Arrange

        // Mocking servient.addThing to return true, indicating the Thing is added successfully
        every { servient.addThing(ofType(ExposedThing::class)) } returns true

        // Act
        val exposedThing = defaultWot.produce{
            title = "testTitle"
            stringProperty("propA") {
                title = "some title"
            }
            intProperty("propB") {
                title = "some title"
            }
        }

        // Assert
        assertEquals(exposedThing.title, "testTitle")
    }

    @Test
    fun `produce should throw WotException when thing already exists`() = runTest {
        // Arrange
        val thing = Thing(id = "existingThing")

        // Mocking servient.addThing to return false, indicating the Thing already exists
        coEvery { servient.addThing(ofType(ExposedThing::class)) } returns false

        // Act and Assert
        val exception = assertFailsWith<WotException> {
            defaultWot.produce(thing)
        }
        assertEquals("Thing already exists: existingThing", exception.message)
    }
}