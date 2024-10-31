package ai.ancf.lmos.wot.thing.event


import ai.ancf.lmos.wot.thing.schema.StringSchema
import app.cash.turbine.test
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class ExposedThingEventTest {
    private var name: String? = null
    private var state: ExposedThingEvent.EventState<*>? = null

    @BeforeTest
    fun setUp() {
        name = "change"
        state = mockk()
    }

    @Test
    fun emitWithoutDataShouldEmitNullAsNextValueToEventState(): Unit = runTest {
        val thing = ThingEvent(data = StringSchema())
        val exposedThingEvent = ExposedThingEvent(thing)

        // Collect emissions and check expected value
        exposedThingEvent.getState().flow.test {
            exposedThingEvent.emit("Event")
            assertEquals("Event", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}