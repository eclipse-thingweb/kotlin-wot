package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

/**
 * Serializes the single context or the list of contexts of a [Thing] to JSON. Is used by
 * Jackson
 */
internal class ContextSerializer : JsonSerializer<Context>() {
    @Throws(IOException::class)
    override fun serialize(
        context: Context,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        val defaultUrl: String? = context.defaultUrl
        val prefixedUrls: Map<String, String> = context.prefixedUrls
        val hasDefaultUrl = defaultUrl != null
        val hasPrefixedUrls = prefixedUrls.isNotEmpty()
        if (hasDefaultUrl && hasPrefixedUrls) {
            gen.writeStartArray()
        }
        if (hasDefaultUrl) {
            gen.writeString(defaultUrl)
        }
        if (hasPrefixedUrls) {
            gen.writeObject(prefixedUrls)
        }
        if (hasDefaultUrl && hasPrefixedUrls) {
            gen.writeEndArray()
        }
    }
}
