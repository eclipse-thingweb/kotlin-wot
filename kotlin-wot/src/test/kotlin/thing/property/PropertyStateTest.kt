package ai.ancf.lmos.wot.thing.property

/*
class PropertyStateTest {

    // Test initial value
    @Test
    fun `initial value should be set correctly`() {
        val propertyState = PropertyState(initialValue = 42)

        assertEquals(42, propertyState.value)
    }

    // Test setting a new value
    @Test
    fun `setValue should update the value and emit correctly`() = runTest {
        val propertyState = PropertyState(initialValue = 0)

        // Collect emitted values using Turbine
        propertyState.flow.test {
            // Expect the initial value to be emitted
            assertEquals(0, awaitItem())

            // Set a new value and expect it to be emitted
            propertyState.setValue(10)
            assertEquals(10, awaitItem())

            // Set another new value and expect it to be emitted
            propertyState.setValue(20)
            assertEquals(20, awaitItem())

            cancelAndIgnoreRemainingEvents() // Cancel after the test
        }
    }


    @Test
    fun `readHandler should return correct value`() = runTest {
        val propertyState = PropertyState(0, readHandler = { 100 })

        assertEquals(100, propertyState.readHandler?.handle())
    }


    @Test
    fun `writeHandler should set the value correctly`() = runTest {
        val propertyState = PropertyState(0) { input -> input }

        val newValue = propertyState.writeHandler?.handle(50)
        propertyState.setValue(newValue)
        assertEquals(50, propertyState.value)
    }
}
*/
