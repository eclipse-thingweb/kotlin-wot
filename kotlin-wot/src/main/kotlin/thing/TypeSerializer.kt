package ai.ancf.lmos.wot.thing

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException


class TypeSerializer : JsonSerializer<Type>() {

    @Throws(IOException::class)
    override fun serialize(type: Type, gen: JsonGenerator, serializers: SerializerProvider) {
        val types = type.types
        if (types.size == 1) {
                gen.writeString(types.iterator().next())
            } else if (types.size > 1) {
                gen.writeStartArray()
                for (t in types) {
                    gen.writeString(t)

                }
                gen.writeEndArray()
            }
    }
}
