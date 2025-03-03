/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing

import org.eclipse.thingweb.JsonMapper
import org.eclipse.thingweb.thing.schema.Type
import com.fasterxml.jackson.core.JsonProcessingException
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypeTest {
    @Test
    @Throws(IOException::class)
    fun fromJson() {
        // single value
        assertEquals(
            Type("Thing"),
            JsonMapper.instance.readValue("\"Thing\"", Type::class.java)
        )

        // array
        assertEquals(
            Type("Thing").addType("saref:LightSwitch"),
            JsonMapper.instance.readValue("[\"Thing\",\"saref:LightSwitch\"]", Type::class.java)
        )
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun toJson() {
        // single value
        assertEquals(
            "\"Thing\"",
            JsonMapper.instance.writeValueAsString(Type("Thing"))
        )

        // multi type array
        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(Type("Thing").addType("saref:LightSwitch")))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isArray()
            .contains("Thing", "saref:LightSwitch")
    }
}