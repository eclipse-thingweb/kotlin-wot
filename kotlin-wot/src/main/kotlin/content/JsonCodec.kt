/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException
import kotlin.reflect.KClass

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
    ): JsonNode {
        return try {
            JsonMapper.instance.readTree(body)
        } catch (e: IOException) {
            throw ContentCodecException("Failed to decode $mediaType: ${e.message}", e)
        }
    }

    override fun <O : Any> bytesToValue(body: ByteArray, parameters: Map<String, String>, clazz: KClass<O>): O {
        return try {
            JsonMapper.instance.readValue(body, clazz.java)
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
        value: JsonNode,
        parameters: Map<String, String>
    ): ByteArray {
        try{
        return JsonMapper.instance.writeValueAsBytes(value)
        } catch (e: JsonProcessingException) {
            throw ContentCodecException("Failed to encode $mediaType: $e")
        }
    }
}
