package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSchemaTest {

    private val mapper = JsonMapper.instance

    @Test
    fun `should deserialize to StringSchema`() {
        val json = """{"type": "string", "default": "test"}"""
        val schema: StringSchema = mapper.readValue(json)
        assertEquals(schema.default, "test")
    }

    @Test
    fun `should deserialize to IntegerSchema`() {
        val json = """{"type": "integer", "default": 1}"""
        val schema: IntegerSchema = mapper.readValue(json)
        assertEquals(schema.default, 1)
    }

    @Test
    fun `should deserialize to NumberSchema`() {
        val json = """{"type": "number", "default": 1.2}"""
        val schema: NumberSchema = mapper.readValue(json)
        assertEquals(schema.default, 1.2)
    }

    @Test
    fun `should deserialize to BooleanSchema`() {
        val json = """{"type": "boolean", "default": true}"""
        val schema: BooleanSchema = mapper.readValue(json)
        assertEquals(schema.default, true)
    }

    @Test
    fun `should deserialize to ArraySchema`() {
        val json = """{
            "type": "array", 
            "default": [1,2], 
            "items": [
                {
                  "type": "integer"
                }
              ],
            "minItems": 1,
            "maxItems": 10}"""
        val schema: ArraySchema<Int> = mapper.readValue(json)
        assertEquals(schema.default, listOf(1,2))
    }

    @Test
    fun `should deserialize to ObjectSchema`() {
        val json = """"{
                "type": "object",
                "properties": {
                    "from": {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 100
                    },
                    "to": {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 100
                    },
                    "duration": {
                        "type": "number"
                    }
                },
                "required": ["to","duration"]
            }"""
        val schema: ObjectSchema = mapper.readValue(json)
        assertEquals(3, schema.properties.size)
        assertEquals(2, schema.required.size)
    }

    @Test
    fun `should deserialize to NullSchema`() {
        val json = """{"type": "null"}"""
        val schema: NullSchema = mapper.readValue(json)
    }
}