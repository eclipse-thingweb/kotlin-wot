package ai.ancf.lmos.wot.thing.schema

import kotlin.test.Test
import kotlin.test.assertEquals

class DataSchemaValueTest {
    @Test
    fun `should return string`() {
        val stringValue = DataSchemaValue.StringValue("test")

        assertEquals("test", stringValue.value)
    }
}