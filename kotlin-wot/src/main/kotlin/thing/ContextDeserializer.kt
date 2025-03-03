/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing

import org.eclipse.thingweb.thing.schema.Context
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Deserializes the individual context or the list of contexts of a [ThingDescription] from JSON. Is used
 * by Jackson
 */
internal class ContextDeserializer : JsonDeserializer<Context?>() {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Context? {
        val t = p.currentToken()
        return if (t == JsonToken.VALUE_STRING) {
            Context(p.valueAsString)
        } else if (t == JsonToken.START_ARRAY) {
            val context = Context()
            val arrayNode = p.codec.readTree<ArrayNode>(p)
            val arrayElements = arrayNode.elements()
            while (arrayElements.hasNext()) {
                val arrayElement = arrayElements.next()
                if (arrayElement is TextNode) {
                    context.addContext(arrayElement.asText())
                } else if (arrayElement is ObjectNode) {
                    val objectEntries = arrayElement.fields()
                    while (objectEntries.hasNext()) {
                        val (prefix, value) = objectEntries.next()
                        val url = value.asText()
                        context.addContext(prefix, url)
                    }
                }
            }
            context
        } else {
            log.warn("Unable to deserialize Context of type '{}'", t)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ContextDeserializer::class.java)
    }
}
