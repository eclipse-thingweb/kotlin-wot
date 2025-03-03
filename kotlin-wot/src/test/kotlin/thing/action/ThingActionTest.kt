/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.StringSchema
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals


class ThingActionTest {
    @Test
    fun testEquals() {
        val input= StringSchema()
        val output= StringSchema()

        val action1 = ThingAction(input = input, output = output)
        val action2 = ThingAction(input = input, output = output)
        assertEquals(action1, action2)
    }

    @Test
    fun testHashCode() {
        val input= StringSchema()
        val output= StringSchema()

        val action1 = ThingAction(input = input, output = output).hashCode()
        val action2 = ThingAction(input = input, output = output).hashCode()
        assertEquals(action1, action2)
    }

    @Test
    fun testToJson() {
        val action = ThingAction(
            title = "title",
            description = "blabla",
            input = StringSchema(),
            output = StringSchema())
        val json = JsonMapper.instance.writeValueAsString(action)

        JsonAssertions.assertThatJson(json)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                    "title":"title",
                    "description":"blabla",
                    "input":{"type":"string"},
                    "output":{"type":"string"}
                    }
                """
            )
    }

    @Test
    fun fromJson() {
        val json =  """{
                    "title":"title",
                    "description":"blabla",
                    "input":{"type":"string"},
                    "output":{"type":"string"}
                    }
                """

        val parsedAction = JsonMapper.instance.readValue<ThingAction<String, String>>(json)
        val action = ThingAction(
            title = "title",
            description = "blabla",
            input = StringSchema(),
            output = StringSchema())
        assertEquals(action, parsedAction)
    }


}