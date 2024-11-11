package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.thing.schema.*
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue.*
import java.nio.charset.Charset

/**
 * (De)serializes data in plaintext format.
 */
open class TextCodec : ContentCodec {
    override val mediaType: String
        get() = "text/plain"

    override fun bytesToValue(body: ByteArray, schema: DataSchema<*>?, parameters: Map<String, String>): DataSchemaValue {
        val parsed = parameters["charset"]?.let { charset ->
            String(body, Charset.forName(charset))
        } ?: String(body)

        return when (schema) {
            is BooleanSchema -> BooleanValue(parsed.toBoolean())
            is IntegerSchema -> IntegerValue(parsed.toInt())
            is NumberSchema -> IntegerValue(parsed.toInt())
            is StringSchema -> StringValue(parsed)
            else -> throw IllegalArgumentException("Unsupported schema type: ${schema.toString()}")
        }
    }

    override fun valueToBytes(value: Any, parameters: Map<String, String>): ByteArray {
        val charset = parameters["charset"]
        return if (charset != null) {
            value.toString().toByteArray(Charset.forName(charset))
        } else {
            value.toString().toByteArray()
        }
    }

    override fun valueToBytes(
        value: DataSchemaValue,
        parameters: Map<String, String>
    ): ByteArray {
        return when (value) {
            is StringValue -> value.value.toByteArray()
            is IntegerValue -> value.value.toString().toByteArray()
            is NumberValue ->  value.value.toString().toByteArray()
            is BooleanValue -> value.value.toString().toByteArray()
            is ArrayValue -> value.value.toString().toByteArray()
            is ObjectValue -> value.value.toString().toByteArray()
            is NullValue -> "".toByteArray()
        }
    }
}
