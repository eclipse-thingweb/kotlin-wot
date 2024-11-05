package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.thing.schema.*
import java.nio.charset.Charset

/**
 * (De)serializes data in plaintext format.
 */
open class TextCodec : ContentCodec {
    override val mediaType: String
        get() = "text/plain"

    override fun <T> bytesToValue(body: ByteArray, schema: DataSchema<T>, parameters: Map<String, String>): T {
        val parsed = parameters["charset"]?.let { charset ->
            String(body, Charset.forName(charset))
        } ?: String(body)

        return when (schema) {
            is BooleanSchema -> parsed.toBoolean()
            is IntegerSchema -> parsed.toInt()
            is NumberSchema -> parsed.toDoubleOrNull()?.takeIf { parsed.contains(".") } ?: parsed.toLong()
            is StringSchema -> parsed
            else -> throw IllegalArgumentException("Unsupported schema type: ${schema::class}")
        } as T
    }

    override fun valueToBytes(value: Any, parameters: Map<String, String>): ByteArray {
        val charset = parameters["charset"]
        return if (charset != null) {
            value.toString().toByteArray(Charset.forName(charset))
        } else {
            value.toString().toByteArray()
        }
    }
}
