/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing


import org.eclipse.thingweb.thing.schema.Context
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class ContextTest {

    private val jsonMapper = ObjectMapper()

    /*
    @Test
    @Throws(JsonProcessingException::class)
    fun `test default url`() {
        val jsonString = Json.encodeToString(ContextSerializer, Context("http://www.w3.org/ns/td"))
        // single value
        assertEquals("\"http://www.w3.org/ns/td\"", jsonString)

        val deserializedContext = Json.decodeFromString(ContextSerializer, jsonString)
    }

    @Test
    fun `test with multiple context` () {
        val context = Context("http://www.w3.org/ns/td")
            .addContext("saref", "https://w3id.org/saref#")

        // Serialization
        val jsonString = Json.encodeToString(ContextSerializer, context)
        println(jsonString)

        assertEquals(
            "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
            jsonString
        )

        // Deserialization
        val deserializedContext = Json.decodeFromString(ContextSerializer, jsonString)

    }
    */

    @Test
    @Throws(IOException::class)
    fun fromJson() {
        // single value
        assertEquals(
            Context("http://www.w3.org/ns/td"),
            jsonMapper.readValue<Context>(
                "\"http://www.w3.org/ns/td\"",
                Context::class.java
            )
        )

        // array
        assertEquals(
            Context("http://www.w3.org/ns/td"),
            jsonMapper.readValue(
                "[\"http://www.w3.org/ns/td\"]",
                Context::class.java
            )
        )

        // multi type array
        assertEquals(
            Context("http://www.w3.org/ns/td")
                .addContext("saref", "https://w3id.org/saref#"),
            jsonMapper.readValue(
                "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
                Context::class.java
            )
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun toJson() {
        // single value
        assertEquals(
            "\"http://www.w3.org/ns/td\"",
            jsonMapper.writeValueAsString(Context("http://www.w3.org/ns/td"))
        )

        // multi type array
        assertEquals(
            "[\"http://www.w3.org/ns/td\",{\"saref\":\"https://w3id.org/saref#\"}]",
            jsonMapper.writeValueAsString(
                Context("http://www.w3.org/ns/td")
                    .addContext("saref", "https://w3id.org/saref#")
            )
        )
    }
}