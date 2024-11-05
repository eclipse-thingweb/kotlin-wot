package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException

/**
 * (De)serializes data in JSON format.
 */
open class JsonCodec : ContentCodec {
    override val mediaType: String
        get() = "application/json"

    override fun <T> bytesToValue(
        body: ByteArray,
        schema: DataSchema<T>,
        parameters: Map<String, String>
    ): T {
        return try {
            JsonMapper.instance.readValue(body, schema.classType)
        } catch (e: IOException) {
            throw ContentCodecException("Failed to decode $mediaType: ${e.message}", e)
        }
    }

    override fun valueToBytes(
        value: Any,
        parameters: Map<String, String>
    ): ByteArray {
        return try {
            JsonMapper.instance.writeValueAsBytes(value)
        } catch (e: JsonProcessingException) {
            throw ContentCodecException("Failed to encode $mediaType: $e")
        }
    }
}
