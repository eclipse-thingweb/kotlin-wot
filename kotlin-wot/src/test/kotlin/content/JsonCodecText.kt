package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.thing.schema.BooleanSchema
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.*
import ai.ancf.lmos.wot.thing.schema.IntegerSchema
import ai.ancf.lmos.wot.thing.schema.ObjectSchema
import ai.ancf.lmos.wot.thing.schema.StringSchema
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JsonCodecTest {

    private lateinit var jsonCodec: JsonCodec

    @BeforeEach
    fun setup() {
        jsonCodec = JsonCodec()
    }

    @Test
    fun `mediaType should return application json`() {
        assertEquals("application/json", jsonCodec.mediaType)
    }

    @Test
    fun `bytesToValue should decode JSON byte array to string successfully`() {

        // Act
        val result = jsonCodec.bytesToValue(""""test"""".toByteArray(), StringSchema(), emptyMap())

        result as StringValue

        // Assert
        assertEquals("test", result.value)
    }

    @Test
    fun `bytesToValue should decode JSON byte array to boolean successfully`() {

        // Act
        val result = jsonCodec.bytesToValue("""true""".toByteArray(), BooleanSchema(), emptyMap())

        result as BooleanValue

        // Assert
        assertEquals(true, result.value)
    }

    @Test
    fun `bytesToValue should decode JSON byte array to integer successfully`() {

        // Act
        val result = jsonCodec.bytesToValue("""1""".toByteArray(), IntegerSchema(), emptyMap())

        result as IntegerValue

        // Assert
        assertEquals(1, result.value)
    }


    @Test
    fun `bytesToValue should throw ContentCodecException on invalid JSON`() {
        // Arrange
        val invalidJsonBytes = """{"key": "value"""".toByteArray() // Missing closing brace

        // Act & Assert
        val exception = assertThrows<ContentCodecException> {
            jsonCodec.bytesToValue(invalidJsonBytes, ObjectSchema(), emptyMap())
        }
        assertTrue(exception.message!!.contains("Failed to decode"))
    }

    @Test
    fun `valueToBytes should encode json object to map successfully`() {
        val json = """{"key": "value"}""".toByteArray()
        // Act
        val result = jsonCodec.bytesToValue(json, ObjectSchema(), emptyMap())

        result as ObjectValue

        // Assert
        assertEquals("value", result.value["key"])
    }



    @Test
    fun `valueToBytes should encode StringValue to JSON byte array`() {
        val value = StringValue("test")
        val result = jsonCodec.valueToBytes(value, emptyMap())
        assertArrayEquals(""""test"""".toByteArray(), result)
    }

    @Test
    fun `valueToBytes should encode IntegerValue to JSON byte array`() {
        val value = IntegerValue(1)
        val result = jsonCodec.valueToBytes(value, emptyMap())
        assertArrayEquals("1".toByteArray(), result)
    }

    @Test
    fun `valueToBytes should encode NumberValue to JSON byte array`() {
        val value = NumberValue(1.23)
        val result = jsonCodec.valueToBytes(value, emptyMap())
        assertArrayEquals("1.23".toByteArray(), result)
    }

    @Test
    fun `valueToBytes should encode BooleanValue to JSON byte array`() {
        val value = BooleanValue(true)
        val result = jsonCodec.valueToBytes(value, emptyMap())
        assertArrayEquals("true".toByteArray(), result)
    }

    @Test
    fun `valueToBytes should encode ArrayValue to JSON byte array`() {
        val value = ArrayValue(listOf("test", 1))
        val result = jsonCodec.valueToBytes(value, emptyMap())
        assertArrayEquals("""["test",1]""".toByteArray(), result)
    }

    @Test
    fun `valueToBytes should encode ObjectValue to JSON byte array`() {
        val value = ObjectValue(mapOf("bla" to "blub"))
        val result = jsonCodec.valueToBytes(value, emptyMap())

        assertArrayEquals("""{"bla":"blub"}""".toByteArray(), result)
    }
}