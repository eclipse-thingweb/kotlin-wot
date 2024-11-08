package ai.ancf.lmos.wot.thing.action

/*
class ConsumedThingActionTest {

    private val thing: ConsumedThingImpl = mockk(relaxed = true)
    private val action: ThingAction<String, String> = mockk()
    private val client: ProtocolClient = mockk()
    private val form: Form = Form("test", "application/json")
    private val parameters: Map<String, Any> = mapOf("key" to "value")
    private val inputContent: Content = Content(type = "application/json", body = """{"key": "value"}""".toByteArray())
    private val resultContent: Content = Content(type = "application/json", body = """{"result": "success"}""".toByteArray())
    private val actionState = ExposedThingAction.ActionState<String, String>()

    @Test
    fun `invoke without parameters should return expected output`() = runTest {
        // Arrange
        val actionName = "testAction"
        val expectedOutput = "success"
        val consumedThingAction = ConsumedThingAction(action, thing, actionState)

        every { action.title } returns actionName
        every { consumedThingAction.forms } returns mutableListOf(form)
        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)
        coEvery { client.invokeResource(form, null) } returns resultContent

        // Act
        val actualOutput = consumedThingAction.invoke()

        // Assert
        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `invoke with parameters should return expected output`() = runTest {
        // Arrange
        val actionName = "testAction"
        val expectedOutput = "success"
        val consumedThingAction = ConsumedThingAction(action, thing, actionState)

        every { action.title } returns actionName
        every { consumedThingAction.forms } returns mutableListOf(form)
        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)
        coEvery { client.invokeResource(form, inputContent) } returns resultContent

        // Act
        val actualOutput = consumedThingAction.invoke(parameters)

        // Assert
        assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `invoke should handle exceptions and throw ConsumedThingException`() = runTest {
        // Arrange
        val actionName = "testAction"
        val consumedThingAction = ConsumedThingAction(action, thing, actionState)
        every { consumedThingAction.forms } returns mutableListOf(form)
        every { action.title } returns actionName
        every { thing.getClientFor(any<List<Form>>(), any()) } returns Pair(client, form)

        coEvery { client.invokeResource(any(), any()) } throws ProtocolClientException("Error")

        // Act & Assert
        try {
            consumedThingAction.invoke()
        } catch (e: ConsumedThingException) {
            assertEquals("Client error", e.message)
        }
    }
}
*/
