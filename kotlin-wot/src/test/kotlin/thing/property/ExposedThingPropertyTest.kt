package ai.ancf.lmos.wot.thing.property

import ai.ancf.lmos.wot.thing.Thing
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals

class ExposedThingPropertyTest {

    private lateinit var thing: Thing
    private lateinit var state: ExposedThingProperty.PropertyState<String>

    @BeforeEach
    fun setUp() {
        thing = Thing(id = "testThing")
        state = mockk()
    }

    @Test
    fun testEquals() {
        val property1 = ExposedThingProperty(ThingProperty<Any>(title = "title"), thing)
        val property2 = ExposedThingProperty(ThingProperty<Any>(title = "title"), thing)
        assertEquals(property1, property2)
    }

    @Test
    fun testHashCode() {
        val property1 = ExposedThingProperty(ThingProperty<Any>(title = "title"), thing)
        val property2 = ExposedThingProperty(ThingProperty<Any>(title = "title"), thing)
        assertEquals(property1, property2)
    }

    @Test
    fun readWithoutHandlerShouldReturnStateValue() = runTest {
        every { state.readHandler } returns null
        coEvery { state.value } returns "test"

        val exposedProperty = ExposedThingProperty(
            mockk(relaxed = true),
            thing,
            state
        )

        val value = exposedProperty.read()

        assertEquals("test", value)
    }

    @Test
    fun readWithHandlerShouldCallHandler() = runTest {
        val readHandler: suspend () -> String? = mockk()

        every { state.readHandler } returns readHandler
        coEvery { readHandler() } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedThingProperty(
            mockk(relaxed = true),
            thing,
            state
        )

        val value = exposedProperty.read()

        assertEquals("test", value)
    }


    @Test
    fun readWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val readHandler: suspend () -> String? = mockk()

        coEvery { readHandler() } throws IOException()
        every { state.readHandler } returns readHandler

        val exposedProperty = ExposedThingProperty(
            mockk(relaxed = true),
            thing,
            state
        )

        assertThrows<IOException> { exposedProperty.read() }
    }

    @Test
    fun writeWithoutHandlerShouldSetStateValue() = runTest{
        val writeHandler: suspend (String) -> String? = mockk()

        every { state.writeHandler } returns null
        coEvery { writeHandler(any()) } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedThingProperty(
            mockk(relaxed = true),
            thing,
            state
        )

        val value = exposedProperty.write("test")

        assertEquals("test", value)
    }

    @Test
    fun writeWithHandlerShouldCallHandler() = runTest{
        val writeHandler: suspend (String) -> String? = mockk()

        every { state.writeHandler } returns writeHandler
        coEvery { writeHandler(any()) } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedThingProperty(
            mockk(relaxed = true),
            thing,
            state
        )

        val value = exposedProperty.write("test")

        assertEquals("test", value)
    }

    @Test
    fun writeWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val writeHandler: suspend (String) -> String? = mockk()

        coEvery { writeHandler(any()) } throws IOException()
        every { state.writeHandler } returns writeHandler

        val exposedProperty = ExposedThingProperty(
            mockk(relaxed = true),
            thing,
            state
        )

        assertThrows<IOException> { exposedProperty.write("test") }
    }

}