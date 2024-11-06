package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.thing.ExposedThing
import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import ai.anfc.lmos.wot.binding.ProtocolServer
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ServientTest {

    private val mockServer1 = mockk<ProtocolServer>(relaxed = true)
    private val mockServer2 = mockk<ProtocolServer>(relaxed = true)
    private val factoryMock = mockk<ProtocolClientFactory>(relaxed = true)
    private val mockThing = mockk<ExposedThing>(relaxed = true) {
        every { id } returns "testThing"
    }
    private val servient = Servient(
        servers = listOf(mockServer1, mockServer2),
        clientFactories = listOf(factoryMock),
        things = mutableMapOf("testThing" to mockThing)
    )

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
}