/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.form

import org.eclipse.thingweb.JsonMapper
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals

class FormTest {
    @Test
    fun testForm() {
        val form = Form(
            href = "test:/foo",
            op = listOf( Operation.OBSERVE_PROPERTY),
            subprotocol = "longpolling",
            contentType = "application/json"
        )
        assertEquals("test:/foo", form.href)
        assertEquals(listOf(Operation.OBSERVE_PROPERTY), form.op)
        assertEquals("longpolling", form.subprotocol)
        assertEquals("application/json", form.contentType)

        val json = JsonMapper.instance.writeValueAsString(form)

        JsonAssertions.assertThatJson(json)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"href":"test:/foo",
                    "op":["observeproperty"],
                    "subprotocol":"longpolling",
                    "contentType":"application/json"}
                """
            )
    }
}