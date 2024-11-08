package ai.ancf.lmos.wot.thing.property

/*
class ConsumedThingPropertyTest {

    private val thing: ConsumedThingImpl = mockk(relaxed = true)
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
*/
