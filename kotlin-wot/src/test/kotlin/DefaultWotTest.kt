package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.filter.DiscoveryMethod
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import java.net.URI
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class DefaultWotTest {

    // Mocked dependency
    private lateinit var servient: Servient
    private lateinit var defaultWot: DefaultWot

    @BeforeTest
    fun setup() {
        servient = mockk()  // Mock the Servient class
        defaultWot = DefaultWot(servient)
    }

    @Test
    fun `test discover with filter`() = runBlocking {
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
    fun `test discover without filter`() = runBlocking {
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
    fun `test fetch with URI`() = runBlocking {
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
    fun `test fetch with String URL`() = runBlocking {
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
    fun `test destroy`() = runBlocking {
        // Given
        coEvery { servient.shutdown() } just runs

        // When
        defaultWot.destroy()

        // Then
        coVerify { servient.shutdown() }
    }
}