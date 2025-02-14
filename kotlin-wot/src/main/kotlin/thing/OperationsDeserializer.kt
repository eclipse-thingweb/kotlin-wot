package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.form.Operation
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class OperationsDeserializer : JsonDeserializer<List<Operation>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Operation> {
        val node: JsonNode = p.codec.readTree(p)

        return when {
            node.isTextual -> listOf(Operation.fromJsonValue(node.asText()))
            node.isArray -> node.map { Operation.fromJsonValue(it.asText()) }
            else -> emptyList()
        }
    }
}