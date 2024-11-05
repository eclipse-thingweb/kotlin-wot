package ai.ancf.lmos.wot.thing.property

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.action.ConsumedThingException
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.ThingProperty
import ai.ancf.lmos.wot.thing.schema.stringProperty
import ai.anfc.lmos.wot.binding.ProtocolClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConsumedThingPropertyTest {

    private val thing: ConsumedThing = mockk(relaxed = true)
    private val property: ThingProperty<String> = stringProperty {
        title = "test"
        forms = mutableListOf(Form("test", "application/json"))
    }
    private val client: ProtocolClient = mockk()
    private val form: Form = Form("test", "application/json")
    private val content: Content = Content(type = "application/json", body = """"value"""".toByteArray())
    private val propertyState = ExposedThingProperty.PropertyState<String>()

    private val testProperty = ConsumedThingProperty(property, thing, propertyState)

    @Test
    fun `read should return expected value`() = runTest {

        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)
        coEvery { client.readResource(form) } returns content

        // Act
        val result = testProperty.read()

        // Assert
        assertEquals("value", result)
        coEvery { client.readResource(form) }
    }

    @Test
    fun `write should return expected value`() = runTest {
        // Arrange
        val inputValue = "value"
        val expectedOutputValue = "value"

        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)
        coEvery { client.writeResource(form, any()) } returns content

        // Act
        val result = testProperty.write(inputValue)

        // Assert
        assertEquals(expectedOutputValue, result)
    }

    @Test
    fun `read should throw ConsumedThingException on ContentCodecException`() = runTest {
        // Arrange
        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)
        coEvery { client.readResource(form) } returns Content(type = "application/json", body = "{value".toByteArray())

        // Act & Assert
        assertThrows<ConsumedThingException> {
            testProperty.read()
        }
    }

    @Test
    fun `write should throw ConsumedThingException on ContentCodecException`() = runTest {
        // Arrange
        val inputValue = "value"

        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)
        coEvery { client.writeResource(form, any()) } returns Content(type = "application/json", body = "{value".toByteArray())

        // Act & Assert
        assertThrows<ConsumedThingException> {
            testProperty.write(inputValue)
        }
    }
}