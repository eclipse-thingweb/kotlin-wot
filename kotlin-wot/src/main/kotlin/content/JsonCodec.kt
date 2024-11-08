package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.*
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.*
import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException

/**
 * (De)serializes data in JSON format.
 */
open class JsonCodec : ContentCodec {

    override val mediaType: String
        get() = "application/json"

    override fun bytesToValue(
        body: ByteArray,
        schema: DataSchema<*>,
        parameters: Map<String, String>
    ): DataSchemaValue {
        try {
             val response = when (schema) {
                 is StringSchema -> {
                     // Parse as StringValue
                     JsonMapper.instance.readValue(body, String::class.java).let { StringValue(it) }
                 }

                 is IntegerSchema -> {
                     // Parse as IntegerValue
                     JsonMapper.instance.readValue(body, Int::class.java).let { IntegerValue(it) }
                 }

                 is NumberSchema -> {
                     // Parse as NumberValue
                     JsonMapper.instance.readValue(body, Number::class.java).let { NumberValue(it) }
                 }

                 is BooleanSchema -> {
                     // Parse as BooleanValue
                     JsonMapper.instance.readValue(body, Boolean::class.java).let { BooleanValue(it) }
                 }

                 is ArraySchema<*> -> {
                     // Parse as ArrayValue
                     JsonMapper.instance.readValue(body, List::class.java).let {
                         ArrayValue(it as List<*>)
                     }
                 }

                 is ObjectSchema -> {
                     // Parse as ObjectValue
                     JsonMapper.instance.readValue(body, Map::class.java).let {
                         ObjectValue(it as Map<*, *>)

                     }
                 }

                 is NullSchema -> {
                     // Return Null DataSchemaValue
                     Null
                 }

                 else -> {
                     throw ContentCodecException("Unsupported schema type: $schema")
                 }
             }

            return response

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
