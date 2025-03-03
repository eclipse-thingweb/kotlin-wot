/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.property


import org.eclipse.thingweb.JsonMapper
import org.eclipse.thingweb.thing.schema.Type
import org.eclipse.thingweb.thing.schema.*
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThingPropertyTest {
    @Test
    fun testEquals() {
        val property1 = StringProperty().apply { title = "title" }
        val property2 = StringProperty().apply { title = "title" }
        assertEquals(property1, property2)
    }

    @Test
    fun testHashCode() {
        val property1 = StringProperty().apply { title = "title" }.hashCode()
        val property2 = StringProperty().apply { title = "title" }.hashCode()
        assertEquals(property1, property2)
    }

    @Test
    fun toJson() {
        val property = IntProperty().apply {
            objectType= Type("saref:Temperature")
            description = "bla bla"
            observable=true
            readOnly=true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Temperature","description":"bla bla","type":"integer","observable":true,"readOnly":true}"""
            )
    }



    @Test
    fun fromJson() {
        val json = """{"@type":"saref:Temperature","description":"bla bla","type":"string","observable":true,"readOnly":true}"""

        val parsedProperty = JsonMapper.instance.readValue<StringProperty>(json)
        val property = StringProperty().apply {
            objectType = Type("saref:Temperature")
            description = "bla bla"
            observable = true
            readOnly = true
            minLength = 10
        }
        assertEquals(property, parsedProperty)
    }

    @Test
    fun testConstructor() {
        val property = IntProperty().apply {
            objectType= Type("saref:Temperature")
            observable=true
            readOnly=true
            writeOnly=false
        }
        assertEquals("saref:Temperature", property.objectType?.types?.first())
        assertTrue(property.observable)
        assertTrue(property.readOnly)
        assertFalse(property.writeOnly)
    }

    @Test
    fun testBooleanProperty() {
        val property = BooleanProperty().apply {
            objectType = Type("saref:OnOff")
            description = "On or Off"
            observable = true
            readOnly = true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:OnOff","description":"On or Off","type":"boolean","observable":true,"readOnly":true}"""
            )
    }

    @Test
    fun testNumberProperty() {
        val property = NumberProperty().apply {
            objectType = Type("saref:Voltage")
            description = "Voltage level"
            observable = true
            readOnly = true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Voltage","description":"Voltage level","type":"number","observable":true,"readOnly":true}"""
            )
    }

    @Test
    fun testArrayProperty() {
        val property = ArrayProperty<String>().apply {
            objectType = Type("saref:List")
            description = "A list of items"
            observable = true
            readOnly = true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:List","description":"A list of items","type":"array","observable":true,"readOnly":true}"""
            )
    }

    @Test
    fun testNullProperty() {
        val property = NullProperty().apply {
            objectType = Type("saref:Null")
            description = "A null value"
            observable = true
            readOnly = true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Null","description":"A null value","type":"null","observable":true,"readOnly":true}"""
            )
    }

    @Test
    fun testObjectProperty() {
        val property = ObjectProperty().apply {
            objectType = Type("saref:Complex")
            description = "A complex object"
            observable = true
            readOnly = true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Complex","description":"A complex object","type":"object","observable":true,"readOnly":true}"""
            )
    }

    @Test
    fun testObjectPropertyWithSubproperties() {
        val subProperty1 = StringProperty().apply {
            title = "subProperty1"
            description = "A string subproperty"
            readOnly = true
        }

        val subProperty2 = IntProperty().apply {
            title = "subProperty2"
            description = "An integer subproperty"
        }

        val objectProperty = ObjectProperty().apply {
            objectType = Type("saref:Complex")
            description = "A complex object with subproperties"
            properties  += mutableMapOf(
                "subProperty1" to subProperty1,
                "subProperty2" to subProperty2
            )
            observable = true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(objectProperty))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                "@type":"saref:Complex",
                "description":"A complex object with subproperties",
                "type":"object",
                "properties": {
                    "subProperty1": {
                        "title": "subProperty1",
                        "description": "A string subproperty",
                        "type": "string",
                        "readOnly": true
                    },
                    "subProperty2": {
                        "title": "subProperty2",
                        "description": "An integer subproperty",
                        "type": "integer"
                    }
                },
                "observable": true
            }"""
            )
    }

}