package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.thing.Thing
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ExposedThingActionTest {
    private lateinit var thing: Thing
    private lateinit var thingAction: ThingAction<String, String>
    private lateinit var state: ExposedThingAction.ActionState<String, String>

    @BeforeTest
    fun setUp() {
        thing = Thing(id = "test")
        thingAction = ThingAction(title = "title")
        state = mockk()
    }

    @Test
    fun testEquals() {
        val action1 = ExposedThingAction(thingAction, thing)
        val action2 = ExposedThingAction(thingAction, thing)
        assertEquals(action1, action2)
    }

    @Test
    fun testHashCode() {
        val action1 = ExposedThingAction(thingAction, thing).hashCode()
        val action2 = ExposedThingAction(thingAction, thing).hashCode()
        assertEquals(action1, action2)
    }

    @Test
    fun invokeWithoutHandlerShouldReturnNull() = runTest {
        every { state.handler } returns null

        val exposedThingAction = ExposedThingAction(
            thingAction,
            thing,
            state
        )

        // Invoke the action without a handler
        assertNull(exposedThingAction.invoke("test"))
    }

    @Test
    fun invokeWithHandlerShouldCallHandler(): Unit = runTest {
        val handler: (suspend (String, Map<String, Map<String, Any>>) -> String) = mockk()

        every { state.handler } returns handler

        val exposedThingAction = ExposedThingAction(
            thingAction,
            thing,
            state
        )

        coEvery { handler("test", any()) } returns "Result"

        val result = exposedThingAction.invoke("test")
        assertEquals("Result", result)
    }

    @Test
    fun invokeWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val handler: (suspend (String, Map<String, Map<String, Any>>) -> String) = mockk()

        every { state.handler } returns handler

        val exposedThingAction = ExposedThingAction(
            thingAction,
            thing,
            state
        )

        coEvery { handler("test", any()) } throws RuntimeException()

        assertFailsWith<RuntimeException> {
            exposedThingAction.invoke("test")
        }
    }

}