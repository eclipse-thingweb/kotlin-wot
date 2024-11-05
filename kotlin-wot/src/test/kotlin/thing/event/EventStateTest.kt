package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.thing.event.ExposedThingEvent.EventState
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventStateTest {

    @Test
    fun `emit should send value to flow`() = runTest {
        // Arrange
        val eventState = EventState<Int>()

        // Assert
        eventState.flow.test {
            eventState.emit(42)

            assertEquals(42, awaitItem())

            cancelAndIgnoreRemainingEvents() // Cancel after the test
        }
    }

    @Test
    fun `emit multiple values should send all values to flow`() = runTest {
        // Arrange
        val eventState = EventState<Int>()

        // Assert
        eventState.flow.test {
            eventState.emit(1)
            eventState.emit(2)
            eventState.emit(3)

            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents() // Cancel after the test
        }
    }
}