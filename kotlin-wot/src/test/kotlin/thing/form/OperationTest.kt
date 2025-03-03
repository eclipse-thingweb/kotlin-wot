/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing.form

import ai.ancf.lmos.wot.JsonMapper
import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class OperationTest {
    @Test
    @Throws(JsonProcessingException::class)
    fun toJson() {
        val op = Operation.READ_PROPERTY
        val json = JsonMapper.instance.writeValueAsString(op)
        assertEquals("\"readproperty\"", json)
    }

    @Test
    @Throws(IOException::class)
    fun fromJson() {
        val json = "\"writeproperty\""
        val op = JsonMapper.instance.readValue(json, Operation::class.java)
        assertEquals(Operation.WRITE_PROPERTY, op)
    }
}