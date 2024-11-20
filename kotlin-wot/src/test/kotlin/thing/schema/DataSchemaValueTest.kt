package ai.ancf.lmos.wot.thing.schema

import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `toDataSchemaValue should return NullValue for null input`() {
        val result = DataSchemaValue.toDataSchemaValue(null)
        assertEquals(DataSchemaValue.NullValue, result)
    }

    @Test
    fun `toDataSchemaValue should return BooleanValue for boolean input`() {
        val result = DataSchemaValue.toDataSchemaValue(true)
        assertEquals(DataSchemaValue.BooleanValue(true), result)
    }

    @Test
    fun `toDataSchemaValue should return IntegerValue for integer input`() {
        val result = DataSchemaValue.toDataSchemaValue(42)
        assertEquals(DataSchemaValue.IntegerValue(42), result)
    }

    @Test
    fun `toDataSchemaValue should return NumberValue for long input`() {
        val result = DataSchemaValue.toDataSchemaValue(42L)
        assertEquals(DataSchemaValue.NumberValue(42L), result)
    }

    @Test
    fun `toDataSchemaValue should return NumberValue for double input`() {
        val result = DataSchemaValue.toDataSchemaValue(3.14)
        assertEquals(DataSchemaValue.NumberValue(3.14), result)
    }

    @Test
    fun `toDataSchemaValue should return NumberValue for float input`() {
        val result = DataSchemaValue.toDataSchemaValue(3.14f)
        assertEquals(DataSchemaValue.NumberValue(3.14f), result)
    }

    @Test
    fun `toDataSchemaValue should return StringValue for string input`() {
        val result = DataSchemaValue.toDataSchemaValue("test")
        assertEquals(DataSchemaValue.StringValue("test"), result)
    }

    @Test
    fun `toDataSchemaValue should return ObjectValue for map input`() {
        val map = mapOf("key" to "value")
        val result = DataSchemaValue.toDataSchemaValue(map)
        assertEquals(DataSchemaValue.ObjectValue(map), result)
    }

    @Test
    fun `toDataSchemaValue should return ArrayValue for list input`() {
        val list = listOf("item1", "item2")
        val result = DataSchemaValue.toDataSchemaValue(list)
        assertEquals(DataSchemaValue.ArrayValue(list), result)
    }

    @Test
    fun `toDataSchemaValue should throw IllegalArgumentException for unsupported type`() {
        assertFailsWith<IllegalArgumentException> {
            DataSchemaValue.toDataSchemaValue(Any())
        }
    }

}