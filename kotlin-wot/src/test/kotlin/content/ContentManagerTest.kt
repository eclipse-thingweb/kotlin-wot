package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

class ContentManagerTest {

    private val jsonCodec: ContentCodec = mockk(relaxed = true)
    private val schema: DataSchema<String> = mockk()
    private val testContent = Content(type = "application/json", body = """{"key": "value"}""".toByteArray())

    @BeforeEach
    fun setup() {
        // Ensure all interactions with `jsonCodec` are correctly configured
        every { jsonCodec.mediaType } returns "application/json"

        // Reset CODECS and OFFERED maps
        ContentManager.removeCodec("application/json")
        ContentManager.addCodec(jsonCodec, true)
    }

    @Test
    fun `addCodec should register a codec and add to OFFERED if offered is true`() {
        // Arrange
        every { jsonCodec.mediaType } returns "application/json"

        // Act
        ContentManager.addCodec(jsonCodec, true)

        // Assert
        assertTrue(ContentManager.offeredMediaTypes.contains("application/json"))
        assertTrue(ContentManager.isSupportedMediaType("application/json"))
    }

    @Test
    fun `removeCodec should unregister codec and remove from OFFERED set`() {
        // Arrange
        every { jsonCodec.mediaType } returns "application/json"
        ContentManager.addCodec(jsonCodec, true)

        // Act
        ContentManager.removeCodec("application/json")

        // Assert
        assertFalse(ContentManager.offeredMediaTypes.contains("application/json"))
        assertFalse(ContentManager.isSupportedMediaType("application/json"))
    }

    @Test
    fun `contentToValue should use codec to convert content to value`() {
        // Arrange
        val expectedValue = "decoded value"
        every { schema.classType } returns String::class.java
        every { jsonCodec.bytesToValue(testContent.body, schema, any()) } returns DataSchemaValue.StringValue(expectedValue)

        // Act
        val result = ContentManager.contentToValue(testContent, schema)

        result as DataSchemaValue.StringValue

        // Assert
        assertEquals(expectedValue, result.value)
        verify { jsonCodec.bytesToValue(testContent.body, schema, any()) }
    }

    @Test
    fun `contentToValue should throw exception when codec fails to decode`() {
        // Arrange
        every { schema.classType } returns String::class.java
        every { jsonCodec.bytesToValue(testContent.body, schema, any()) } throws ContentCodecException("Decode error")

        // Act & Assert
        val exception = assertThrows<ContentCodecException> {
            ContentManager.contentToValue(testContent, schema)
        }
        assertEquals("Decode error", exception.message)
    }

    @Test
    fun `valueToContent should use codec to serialize value to Content`() {
        // Arrange
        val testValue = "value to encode"
        val expectedContent = Content("application/json", """{"key": "value"}""".toByteArray())
        every { jsonCodec.valueToBytes(testValue, any()) } returns expectedContent.body

        // Act
        val result = ContentManager.valueToContent(testValue, "application/json")

        // Assert
        assertEquals(expectedContent, result)
        verify { jsonCodec.valueToBytes(testValue, any()) }
    }

    @Test
    fun `valueToContent should throw exception when codec fails to encode`() {
        // Arrange
        val testValue = "value to encode"
        every { jsonCodec.valueToBytes(testValue, any()) } throws ContentCodecException("Encode error")

        // Act & Assert
        val exception = assertThrows<ContentCodecException> {
            ContentManager.valueToContent(testValue, "application/json")
        }
        assertEquals("Encode error", exception.message)
    }

    @Test
    fun `fallbackBytesToValue should deserialize content using Java deserialization`() {
        // Arrange
        val byteArray = ByteArrayOutputStream().apply {
            ObjectOutputStream(this).writeObject("Fallback value")
        }.toByteArray()
        val content = Content("application/octet-stream", byteArray)
        every { schema.classType } returns String::class.java

        // Act
        val result = ContentManager.contentToValue(content, schema)

        result as DataSchemaValue.StringValue

        // Assert
        assertEquals("Fallback value", result.value)
    }

    @Test
    fun `fallbackBytesToValue should throw exception on invalid Java deserialization`() {
        // Arrange
        val byteArray = "invalid".toByteArray()
        val content = Content("application/octet-stream", byteArray)
        every { schema.classType } returns String::class.java

        // Act & Assert
        assertThrows<ContentCodecException> {
            ContentManager.contentToValue(content, schema)
        }
    }

    @Test
    fun `getMediaType should extract correct media type`() {
        // Act
        val result = ContentManager.getMediaType("text/plain; charset=utf-8")

        // Assert
        assertEquals("text/plain", result)
    }

    @Test
    fun `getMediaTypeParameters should extract parameters correctly`() {
        // Act
        val result = ContentManager.getMediaTypeParameters("text/plain; charset=utf-8; boundary=something")

        // Assert
        assertEquals(2, result.size)
        assertEquals("utf-8", result["charset"])
        assertEquals("something", result["boundary"])
    }
}