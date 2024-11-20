package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.*
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

/**
 * (De)serializes data in JSON format.
 */
open class JsonCodec : ContentCodec {

    override val mediaType: String
        get() = "application/json"

    override fun bytesToValue(
        body: ByteArray,
        schema: DataSchema<*>?,
        parameters: Map<String, String>
    ): DataSchemaValue {
        return try {
            val result : Any? = JsonMapper.instance.readValue(body)
            DataSchemaValue.toDataSchemaValue(result)
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

    override fun valueToBytes(
        value: DataSchemaValue,
        parameters: Map<String, String>
    ): ByteArray {
        return when (value) {
            is StringValue -> JsonMapper.instance.writeValueAsBytes(value.value)
            is IntegerValue -> JsonMapper.instance.writeValueAsBytes(value.value)
            is NumberValue -> JsonMapper.instance.writeValueAsBytes(value.value)
            is BooleanValue -> JsonMapper.instance.writeValueAsBytes(value.value)
            is ArrayValue -> JsonMapper.instance.writeValueAsBytes(value.value)
            is ObjectValue -> JsonMapper.instance.writeValueAsBytes(value.value)
            is NullValue -> JsonMapper.instance.writeValueAsBytes(null)
        }
    }
}
