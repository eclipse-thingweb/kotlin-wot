package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

/**
 * Serializes the single context or the list of contexts of a [ThingDescription] to JSON. Is used by
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

/*
internal object ContextSerializer : KSerializer<Context> {
    override val descriptor = buildClassSerialDescriptor("Context")
    private val delegateSerializer = MapSerializer(String.serializer(), String.serializer())
    override fun serialize(encoder: Encoder, value: Context) {

        val hasDefaultUrl = value.defaultUrl != null
        val hasPrefixedUrls = value.prefixedUrls.isNotEmpty()

        // Prepare to encode the structure
        if (hasDefaultUrl && hasPrefixedUrls) {
            // Both present: serialize as an array
            encoder.encodeStructure(descriptor) {
                // Encode the default URL
                encoder.encodeString(value.defaultUrl!!)
                // Encode the prefixed URLs
                encoder.encodeSerializableValue(delegateSerializer, value.prefixedUrls)
            }
        } else if (hasDefaultUrl) {
            // Only serialize the default URL as a plain string
            encoder.encodeString(value.defaultUrl!!)
        } else if (hasPrefixedUrls) {
            // Serialize only the prefixed URLs as an object
            encoder.encodeSerializableValue(delegateSerializer, value.prefixedUrls)
        }
    }

    override fun deserialize(decoder: Decoder): Context {
        val compositeInput = decoder.beginStructure(descriptor)
        var defaultUrl: String? = null
        val prefixedUrls = mutableMapOf<String?, String>()

        // Read elements from the structure
        loop@ while (true) {
            when (val index = compositeInput.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> defaultUrl = compositeInput.decodeStringElement(descriptor, index)
                1 -> {
                    // Deserialize the prefixedUrls map
                    prefixedUrls.putAll(compositeInput.decodeSerializableElement(descriptor, index, MapSerializer(String.serializer(), String.serializer())))
                }
                else -> throw SerializationException("Unknown index: $index")
            }
        }

        compositeInput.endStructure(descriptor)

        val context = Context()
        defaultUrl?.let { context.addContext(it) }
        prefixedUrls.forEach { (prefix, url) -> context.addContext(prefix, url) }

        return context
    }
}
*/
