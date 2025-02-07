package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.schema.Type
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import org.slf4j.LoggerFactory
import java.io.IOException


class TypeDeserializer : JsonDeserializer<Type>() {

    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Type? {
        val t = p.currentToken()
        return when (t) {
            JsonToken.VALUE_STRING -> {
                Type(p.valueAsString)
            }
            JsonToken.START_ARRAY -> {
                val type = Type()
                val arrayNode = p.codec.readTree<ArrayNode>(p)
                val arrayElements = arrayNode.elements()
                while (arrayElements.hasNext()) {
                    val arrayElement = arrayElements.next()
                    if (arrayElement is TextNode) {
                        type.addType(arrayElement.asText())
                    }
                }
                type
            }
            else -> {
                log.warn("Unable to deserialize Context of type '{}'", t)
                null
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TypeDeserializer::class.java)
    }
}
