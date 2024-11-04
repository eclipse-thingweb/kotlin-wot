package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.schema.StringSchema
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
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
    fun testToJson() {
        val action = ThingAction(
            title = "title",
            description = "blabla",
            input = StringSchema(),
            output = StringSchema()
        )

        val json = JsonMapper.instance.writeValueAsString(ExposedThingAction(action, thing))

        JsonAssertions.assertThatJson(json)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                    "title":"title",
                    "description":"blabla",
                    "input":{"type":"string"},
                    "output":{"type":"string"}
                    }
                """
            )
    }

    @Test
    fun fromJson() {
        val json = """{
                    "title":"title",
                    "description":"blabla",
                    "input":{"type":"string"},
                    "output":{"type":"string"}
                    }"""

        val parsedThingAction = JsonMapper.instance.readValue<ExposedThingAction<String, String>>(json)
        assertEquals("title", parsedThingAction.title)
        assertEquals("blabla", parsedThingAction.description)
        assertIs<StringSchema>(parsedThingAction.input)
        assertIs<StringSchema>(parsedThingAction.output)
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
        assertNull(exposedThingAction.invokeAction("test"))
    }

    @Test
    fun invokeWithHandlerShouldCallHandler(): Unit = runTest {
        val handler: ActionHandler<String, String> = mockk()

        every { state.handler } returns handler

        val exposedThingAction = ExposedThingAction(
            thingAction,
            thing,
            state
        )

        coEvery { handler.handle("test", any()) } returns "Result"

        val result = exposedThingAction.invokeAction("test")
        assertEquals("Result", result)
    }

    @Test
    fun invokeWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val handler: ActionHandler<String, String> = mockk()

        every { state.handler } returns handler

        val exposedThingAction = ExposedThingAction(
            thingAction,
            thing,
            state
        )

        coEvery { handler.handle("test", any()) } throws RuntimeException()

        assertFailsWith<RuntimeException> {
            exposedThingAction.invokeAction("test")
        }
    }

}