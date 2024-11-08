package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSchemaTest {

    private val mapper = JsonMapper.instance

    @Test
    fun `should deserialize to StringSchema with all properties`() {
        // Sample JSON input with a variety of fields
        val json = """
            {
                "type": "string",
                "title": "test title",
                "default": "test default",
                "minLength": 5,
                "maxLength": 10,
                "pattern": "^[a-z]+$",
                "contentEncoding": "utf-8",
                "contentMediaType": "text/plain",
                "readOnly": true,
                "writeOnly": false
            }
        """.trimIndent()

        // Deserialization
        val schema: StringSchema = mapper.readValue(json)

        // Assertions for deserialization
        assertEquals("test title", schema.title)
        assertEquals("test default", schema.default)
        assertEquals(5, schema.minLength)
        assertEquals(10, schema.maxLength)
        assertEquals("^[a-z]+$", schema.pattern)
        assertEquals("utf-8", schema.contentEncoding)
        assertEquals("text/plain", schema.contentMediaType)
        assertEquals(true, schema.readOnly)
        assertEquals(false, schema.writeOnly)

        // Serialize back to JSON
        val schemaAsJson = mapper.writeValueAsString(schema)

        val jsonWithSkippedDefaultValues = """
            {
                "type": "string",
                "title": "test title",
                "default": "test default",
                "minLength": 5,
                "maxLength": 10,
                "pattern": "^[a-z]+$",
                "contentEncoding": "utf-8",
                "contentMediaType": "text/plain",
                "readOnly": true
            }
        """.trimIndent()

        // Assertions for serialization
        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonWithSkippedDefaultValues)
    }

    @Test
    fun `should handle missing optional fields gracefully`() {
        // JSON with only mandatory fields
        val json = """{"type": "string"}"""

        // Deserialization
        val schema: StringSchema = mapper.readValue(json)

        // Assertions for defaults and null values
        assertEquals(null, schema.title)
        assertEquals(null, schema.default)
        assertEquals(null, schema.minLength)
        assertEquals(null, schema.maxLength)
        assertEquals(null, schema.pattern)
        assertEquals(null, schema.contentEncoding)
        assertEquals(null, schema.contentMediaType)
        assertEquals(false, schema.readOnly)  // Default value for Boolean
        assertEquals(false, schema.writeOnly) // Default value for Boolean

        // Serialize back to JSON and verify no extra fields
        val schemaAsJson = mapper.writeValueAsString(schema)
        val expectedJson = """{"type":"string"}"""

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(expectedJson)
    }

    @Test
    fun `should serialize and deserialize correctly with empty and null values`() {
        // Instance with various empty and null fields
        val schema = StringSchema(
            title = "",
            default = null,
            minLength = null,
            maxLength = null,
            pattern = "",
            contentEncoding = null,
            contentMediaType = null,
            readOnly = false,
            writeOnly = false
        )

        // Serialize to JSON
        val schemaAsJson = mapper.writeValueAsString(schema)

        // Expected JSON excluding fields set to null or empty as per @JsonInclude configuration
        val expectedJson = """{"type":"string"}"""

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(expectedJson)

        // Deserialize back to an object
        val deserializedSchema: StringSchema = mapper.readValue(schemaAsJson)

        // Assertions for deserialization
        assertEquals(null, deserializedSchema.title)
        assertEquals(null, deserializedSchema.default)
        assertEquals(null, deserializedSchema.minLength)
        assertEquals(null, deserializedSchema.maxLength)
        assertEquals(null, deserializedSchema.pattern)
        assertEquals(null, deserializedSchema.contentEncoding)
        assertEquals(null, deserializedSchema.contentMediaType)
        assertEquals(false, deserializedSchema.readOnly)
        assertEquals(false, deserializedSchema.writeOnly)
    }

    @Test
    fun `should deserialize to IntegerSchema`() {
        val json = """{"type": "integer", "title": "test", "default": 1}"""
        val schema: IntegerSchema = mapper.readValue(json)
        assertEquals(schema.default, 1)
        assertEquals(schema.title, "test")

        val schemaAsJson = mapper.writeValueAsString(schema)

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(json)

    }

    @Test
    fun `should deserialize to NumberSchema`() {
        val json = """{"type": "number", "title": "test", "default": 1.2}"""
        val schema: NumberSchema = mapper.readValue(json)
        assertEquals(schema.default, 1.2)
        assertEquals(schema.title, "test")

        val schemaAsJson = mapper.writeValueAsString(schema)

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(json)

    }

    @Test
    fun `should deserialize to BooleanSchema`() {
        val json = """{"type": "boolean", "title": "test", "default": true}"""
        val schema: BooleanSchema = mapper.readValue(json)
        assertEquals(schema.default, true)
        assertEquals(schema.title, "test")

        val schemaAsJson = mapper.writeValueAsString(schema)

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(json)
    }

    @Test
    fun `should deserialize to ArraySchema`() {
        val json = """{
            "type": "array", 
            "title": "test",
            "default": [1,2], 
            "items": {
              "type": "integer"
            },
            "minItems": 1,
            "maxItems": 10}"""
        val schema: ArraySchema<Int> = mapper.readValue(json)
        assertEquals(schema.default, listOf(1,2))
        assertEquals(schema.title, "test")

        val schemaAsJson = mapper.writeValueAsString(schema)

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(json)
    }

    @Test
    fun `should deserialize to ObjectSchema`() {
        val json = """{
            "type": "object",
            "title": "test",
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
        assertEquals(schema.title, "test")
        assertEquals(3, schema.properties.size)
        assertEquals(2, schema.required.size)

        val schemaAsJson = mapper.writeValueAsString(schema)

        println(schemaAsJson)

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(json)
    }

    @Test
    fun `should deserialize to NullSchema`() {
        val json = """{"title":"test", "type": "null"}"""
        val schema: NullSchema = mapper.readValue(json)
        assertEquals(schema.title, "test")

        val schemaAsJson = mapper.writeValueAsString(schema)

        JsonAssertions.assertThatJson(schemaAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(json)
    }
}