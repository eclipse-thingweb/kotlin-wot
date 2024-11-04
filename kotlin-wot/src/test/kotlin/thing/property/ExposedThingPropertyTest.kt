package ai.ancf.lmos.wot.thing.property

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty.*
import ai.ancf.lmos.wot.thing.schema.stringProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.*
import kotlinx.coroutines.test.runTest
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals

class ExposedThingPropertyTest {

    private lateinit var thing: Thing
    private lateinit var state: PropertyState<String>

    @BeforeEach
    fun setUp() {
        thing = Thing(id = "testThing")
        state = mockk()
    }

    @Test
    fun testEquals() {
        val property1 = ExposedStringProperty(thing = thing, state = state, property = stringProperty { title = "title" })
        val property2 = ExposedStringProperty(thing = thing, state = state, property = stringProperty { title = "title" })
        assertEquals(property1, property2)
    }

    @Test
    fun testHashCode() {
        val thing = Thing(id = "Foo")

        val property1 = ExposedStringProperty(thing = thing, state = state, property = stringProperty { title = "title" }).hashCode()
        val property2 = ExposedStringProperty(thing = thing, state = state, property = stringProperty { title = "title" }).hashCode()
        assertEquals(property1, property2)
    }

    @Test
    fun toJson() {
        val property = exposedIntProperty {
            objectType=Type("saref:Temperature")
            description = "bla bla"
            observable=true
            readOnly=true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Temperature","description":"bla bla","type":"integer","observable":true,"readOnly":true}""")
    }

    @Test
    fun fromJson() {
        val json = """{"@type":"saref:Temperature","description":"bla bla","type":"integer","observable":true,"readOnly":true}"""

        val parsedProperty = JsonMapper.instance.readValue<ExposedIntProperty>(json)
        val property = exposedIntProperty {
            objectType=Type("saref:Temperature")
            description = "bla bla"
            observable=true
            readOnly=true
        }
        assertEquals(property, parsedProperty)
    }

    @Test
    fun readWithoutHandlerShouldReturnStateValue() = runTest {
        every { state.readHandler } returns null
        coEvery { state.value } returns "test"

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.read()

        assertEquals("test", value)
    }

    @Test
    fun readWithHandlerShouldCallHandler() = runTest {
        val readHandler: suspend () -> String? = mockk()

        every { state.readHandler } returns readHandler
        coEvery { readHandler() } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.read()

        assertEquals("test", value)
    }


    @Test
    fun readWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val readHandler: suspend () -> String? = mockk()

        coEvery { readHandler() } throws IOException()
        every { state.readHandler } returns readHandler

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        assertThrows<IOException> { exposedProperty.read() }
    }

    @Test
    fun writeWithoutHandlerShouldSetStateValue() = runTest{
        val writeHandler: suspend (String) -> String? = mockk()

        every { state.writeHandler } returns null
        coEvery { writeHandler(any()) } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.write("test")

        assertEquals("test", value)
    }

    @Test
    fun writeWithHandlerShouldCallHandler() = runTest{
        val writeHandler: suspend (String) -> String? = mockk()

        every { state.writeHandler } returns writeHandler
        coEvery { writeHandler(any()) } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.write("test")

        assertEquals("test", value)
    }

    @Test
    fun writeWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val writeHandler: suspend (String) -> String? = mockk()

        coEvery { writeHandler(any()) } throws IOException()
        every { state.writeHandler } returns writeHandler

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        assertThrows<IOException> { exposedProperty.write("test") }
    }

}