package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.thing.schema.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThingDescriptionTest {

    @Test
    fun `test default ID generation`() {
        val newThingDescription = ThingDescription()
        assertTrue(newThingDescription.id.startsWith("urn:uuid:"))
    }

    @Test
    fun toJson() {
        val thingDescription = thingDescription() {
            id = "Foo"
            title = "Test Thing"
            description = "A test thing for unit testing"
            security += "basic_sc"
            //securityDefinitions += "basic_sc" to BasicSecurityScheme("header")
            basicSecurityScheme("basic_sc") {
                `in` = "header"
            }
            stringProperty("stringProperty"){
                title = "propertyTitle"
                enum = listOf("a", "b", "c")
            }
            intProperty("intProperty"){
                title = "propertyTitle"
                exclusiveMaximum = 10
                exclusiveMinimum = 1
            }
            booleanProperty("booleanProperty"){
                title = "propertyTitle"
            }
            numberProperty("numberProperty"){
                title = "propertyTitle"
            }
            objectProperty("objectProperty"){
                title = "propertyTitle"
                stringProperty("subStringProperty"){
                    title = "subPropertyTitle"
                    stringSchema {
                        minLength = 10
                    }
                }
                booleanProperty("subBooleanProperty"){
                    title = "subPropertyTitle"
                    stringSchema {
                        minLength = 10
                    }
                }
            }
            arrayProperty<String>("arrayProperty"){
                title = "propertyTitle"
                items = StringSchema()
                default = listOf("b", "b", "c")
            }
            action<String, Int>("action"){
                title = "actionTitle"
                input = stringSchema {
                    minLength = 10
                    default = "test"
                }
                output = IntegerSchema()
            }
            action<String, Nothing>("action2"){
                title = "actionTitle"
                input = StringSchema()
            }
            event<String, Nothing, Nothing>("event"){
                title = "eventTitle"
                data = StringSchema()
            }
        }

        val thingAsJson = thingDescription.toJson()
        assertThatJson(thingAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                    "id": "Foo",
                    "@type": "Thing",
                    "@context": "https://www.w3.org/2022/wot/td/v1.1",
                    "title": "Test Thing",
                    "description": "A test thing for unit testing",
                    "security": ["basic_sc"],
                    "securityDefinitions":{"basic_sc":{"scheme":"basic","in":"header"}},
                    "properties": {
                        "stringProperty": {
                            "type": "string",
                            "title": "propertyTitle",
                            "enum": [
                                "a",
                                "b",
                                "c"
                            ]
                        },
                        "intProperty": {
                            "type": "integer",
                            "title": "propertyTitle",
                            "exclusiveMinimum": 1,
                            "exclusiveMaximum": 10
                        },
                        "booleanProperty": {
                            "type": "boolean",
                            "title": "propertyTitle"
                        },
                        "numberProperty": {
                            "type": "number",
                            "title": "propertyTitle"
                        },
                        "objectProperty": {
                            "type": "object",
                            "properties": {
                                "subStringProperty": {
                                    "type": "string",
                                    "title": "subPropertyTitle"
                                },
                                "subBooleanProperty": {
                                    "type": "boolean",
                                    "title": "subPropertyTitle"
                                }
                            },
                            "title": "propertyTitle"
                        },
                        "arrayProperty": {
                            "type": "array",
                            "title": "propertyTitle",
                            "default": [
                                "b",
                                "b",
                                "c"
                            ],
                            "items": {
                                "type": "string"
                            }
                        }
                    },
                    "actions": {
                        "action": {
                            "title": "actionTitle",
                            "input": {
                                "type": "string",
                                "default": "test",
                                "minLength": 10
                            },
                            "output": {
                                "type": "integer"
                            }
                        },
                        "action2": {
                            "title": "actionTitle",
                            "input": {
                                "type": "string"
                            }
                        }
                    },
                    "events": {
                        "event": {
                            "title": "eventTitle",
                            "data": {
                                "type": "string"
                            }
                        }
                    }
                }"""
            )
    }

    @Test
    fun fromMap() {
        val map: Map<Any, Any> = mapOf(
            "id" to "Foo",
            "description" to "Bar",
            "@type" to "Thing",
            "@context" to listOf("http://www.w3.org/ns/td")
        )
        val thingDescription: ThingDescription = ThingDescription.fromMap(map)
        assertEquals("Foo", thingDescription.id)
        assertEquals("Bar", thingDescription.description)
        assertEquals(Type("Thing"), thingDescription.objectType)
        assertEquals(
            Context("http://www.w3.org/ns/td"),
            thingDescription.objectContext
        )
    }

    @Test
    fun fromJson() {
        val json = """{
                    "id": "Foo",
                    "@type": "Thing",
                    "@context": "https://www.w3.org/2022/wot/td/v1.1",
                    "title": "Test Thing",
                    "description": "A test thing for unit testing",
                    "security": ["basic_sc"],
                    "securityDefinitions":{"basic_sc":{"scheme":"basic","in":"header"}},
                    "properties": {
                        "stringProperty": {
                            "type": "string",
                            "title": "propertyTitle",
                            "enum": [
                                "a",
                                "b",
                                "c"
                            ]
                        },
                        "intProperty": {
                            "type": "integer",
                            "title": "propertyTitle",
                            "exclusiveMinimum": 1,
                            "exclusiveMaximum": 10
                        },
                        "booleanProperty": {
                            "type": "boolean",
                            "title": "propertyTitle"
                        },
                        "numberProperty": {
                            "type": "number",
                            "title": "propertyTitle"
                        },
                        "objectProperty": {
                            "type": "object",
                            "properties": {
                                "subStringProperty": {
                                    "type": "string",
                                    "title": "subPropertyTitle"
                                },
                                "subBooleanProperty": {
                                    "type": "boolean",
                                    "title": "subPropertyTitle"
                                }
                            },
                            "required": [],
                            "title": "propertyTitle"
                        },
                        "arrayProperty": {
                            "type": "array",
                            "title": "propertyTitle",
                            "default": [
                                "b",
                                "b",
                                "c"
                            ],
                            "items": {
                                "type": "string"
                            }
                        }
                    },
                    "actions": {
                        "action": {
                            "title": "actionTitle",
                            "input": {
                                "type": "string",
                                "default": "test",
                                "minLength": 10
                            },
                            "output": {
                                "type": "integer"
                            }
                        },
                        "action2": {
                            "title": "actionTitle",
                            "input": {
                                "type": "string"
                            }
                        }
                    },
                    "events": {
                        "event": {
                            "title": "eventTitle",
                            "data": {
                                "type": "string"
                            }
                        }
                    }
                }"""
        val thingDescription = ThingDescription.fromJson(json)
        assertEquals("Foo", thingDescription.id)
        assertEquals("A test thing for unit testing", thingDescription.description)
        assertEquals(Type("Thing"), thingDescription.objectType)
        assertEquals(
            Context("https://www.w3.org/2022/wot/td/v1.1"),
            thingDescription.objectContext
        )
        assertEquals(
            thingDescription.securityDefinitions["basic_sc"], BasicSecurityScheme("header")
        )
        assertEquals(listOf("basic_sc"), thingDescription.security)
        assertEquals(6, thingDescription.properties.size)
        assertEquals(2, thingDescription.actions.size)
        assertEquals(1, thingDescription.events.size)
    }

    @Test
    fun testEquals() {
        val thingDescriptionA = ThingDescription(id = "id")
        val thingDescriptionB = ThingDescription(id = "id")
        assertEquals(thingDescriptionA, thingDescriptionB)
    }

    @Test
    fun testHashCode() {
        val thingDescriptionA = ThingDescription(id = "id").hashCode()
        val thingDescriptionB = ThingDescription(id = "id").hashCode()
        assertEquals(thingDescriptionA, thingDescriptionB)
    }

    /*

    @Test
    fun getExpandedObjectType() {
        val thing = Thing(id = "foo",
            description = "Bar",
            objectType =  Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
                .addContext("saref", "https://w3id.org/saref#"))

        assertEquals("https://w3id.org/saref#Temperature",
            thing.getExpandedObjectType("saref:Temperature"))
    }

    @Test
    fun `test getPropertiesByObjectType filters properties by type`() {

        val thing = thing {
            title = "Test Thing"
            description = "A test thing for unit testing"
            property("sensorProperty"){
                objectType = Type("Sensor")
            }
        }

        val result = thing.getPropertiesByObjectType("Sensor")
        assertEquals(1, result.size)
        assertTrue(result.containsKey("sensorProperty"))
    }


     */
}