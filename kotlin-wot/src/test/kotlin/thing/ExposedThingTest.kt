package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedThingTest {


    @Test
    fun testEquals() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ExposedThing(Servient(), thingDescription)
        val thingB = ExposedThing(Servient(), thingDescription)
        assertEquals(thingA, thingB)
    }

    @Test
    fun testHashCode() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ExposedThing(Servient(), thingDescription).hashCode()
        val thingB = ExposedThing(Servient(), thingDescription).hashCode()
        assertEquals(thingA, thingB)
    }


    @Test
    fun toJson() {
        val thingDescription = ThingDescription(
            id  = "foo",
            title = "foo",
            description = "Bla bla",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)

        val thingAsJson = JsonMapper.instance.writeValueAsString(exposedThing)
        JsonAssertions.assertThatJson(thingAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{    
                    "id": "foo",
                    "title":"foo",
                    "description":"Bla bla",
                    "@type":"Thing",
                    "@context":"http://www.w3.org/ns/td"
                }"""
            )
    }

    @Test
    fun shouldDeserializeGivenJsonToThing() {
        val json = """{
                    "id": "Foo",
                    "@type": "Thing",
                    "@context": "https://www.w3.org/2022/wot/td/v1.1",
                    "title": "Test Thing",
                    "description": "A test thing for unit testing",
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
        val thing = ExposedThing.fromJson(json)
        if (thing != null) {
            assertEquals("Foo", thing.id)
            assertEquals("Bar", thing.description)
            assertEquals(Type("Thing"), thing.objectType)
            assertEquals(
                Context("http://www.w3.org/ns/td"),
                thing.objectContext
            )
            assertEquals(
                thing.securityDefinitions["basic_sc"], BasicSecurityScheme("header")
            )
            assertEquals(listOf("basic_sc"), thing.security)
        }
    }
}
