package ai.ancf.lmos.wot.thing.schema

import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSchemaValueTest {
    @Test
    fun `should return string`() {
        val stringValue = DataSchemaValue.StringValue("test")
        assertEquals("test", stringValue.value)
    }

    @Test
    fun `should return integer`() {
        val intValue = DataSchemaValue.IntegerValue(42)
        assertEquals(42, intValue.value)
    }

    @Test
    fun `should return number`() {
        val numberValue = DataSchemaValue.NumberValue(3.14)
        assertEquals(3.14, numberValue.value)
    }

    @Test
    fun `should return boolean`() {
        val booleanValue = DataSchemaValue.BooleanValue(true)
        assertTrue(booleanValue.value)
    }

    @Test
    fun `should return array`() {
        val arrayValue = DataSchemaValue.ArrayValue(listOf(DataSchemaValue.StringValue("item1"), DataSchemaValue.StringValue("item2")))
        assertEquals(listOf(DataSchemaValue.StringValue("item1"), DataSchemaValue.StringValue("item2")), arrayValue.value)
    }

    @Test
    fun `should return object`() {
        val objectValue = DataSchemaValue.ObjectValue(mapOf("key1" to DataSchemaValue.StringValue("value1"), "key2" to DataSchemaValue.IntegerValue(2)))
        assertEquals(mapOf("key1" to DataSchemaValue.StringValue("value1"), "key2" to DataSchemaValue.IntegerValue(2)), objectValue.value)
    }


}