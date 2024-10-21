/*
 * Copyright (c) 2019-2022 Heiko Bornholdt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.security.NoSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ThingProperty
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ThingTest {
    private var objectType: Type? = null
    private var objectContext: Context? = null
    private var id: String = "foo"
    private var title: String? = null
    private var description: String? = null
    private var base: String? = null
    private var titles: Map<String, String>? = null
    private var descriptions: Map<String, String>? = null
    private var properties: Map<Any, ThingProperty<Any>>? = null
    private var actions: Map<Any, ThingAction<Any, Any>>? = null
    private var events: Map<Any, ThingEvent<Any>>? = null
    private var securityDefinitions: Map<Any, SecurityScheme>? = null
    private var forms: List<Form>? = null
    private var security: List<String>? = null
    private var metadata: Map<String, Any>? = null
    @BeforeEach
    fun setUp() {
        objectType = Type("Thing")
        objectContext = Context("http://www.w3.org/ns/td")
        title = "Foo"
        description = "Bar"
        base = ""
        titles = mapOf("de" to "Zähler")
        descriptions = mapOf("de" to "Dies ist ein Zähler")
        properties = emptyMap()
        actions = emptyMap()
        events = emptyMap()
        securityDefinitions = mapOf("basic_sc" to BasicSecurityScheme("header"))
        security = listOf("basic_sc")
        forms = listOf<Form>()
        metadata = mapOf<String, Any>(
            "g:geolocation" to mapOf("position" to mapOf("longitude" to 47.3814, "latitude" to -68.323))
        )
    }

    @Test
    fun toJson() {
        val thing = Thing(
            objectType,
            objectContext,
            id,
            title,
            titles,
            description,
            descriptions
        )
        assertThatJson(thing.toJson())
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{    
                    "id":"foo",
                    "title":"Foo",
                    "titles":{"de":"Zähler"},
                    "description":"Bar",
                    "descriptions":{"de":"Dies ist ein Zähler"},
                    "@type":"Thing",
                    "@context":"http://www.w3.org/ns/td",
                    "securityDefinitions":{"basic_sc":{"scheme":"basic","in":"header"}},
                    "security":["basic_sc"],
                    "g:geolocation":{"position":{"longitude":47.3814,"latitude":-68.323}}
                }"""
            )
    }

    @Nested
    internal inner class FromJson {
        @Test
        fun shouldDeserializeGivenJsonToThing() {
            val json = """{    
                "id":"Foo",
                "description":"Bar",
                "@type":"Thing",
                "@context":["http://www.w3.org/ns/td"],
                "securityDefinitions": {
                    "basic_sc": {
                        "scheme": "basic",
                        "in": "header"
                    }
                },    
                "security": ["basic_sc"]
            }"""
            val thing = Thing.fromJson(json)
            assertEquals("Foo", thing?.id)
            assertEquals("Bar", thing?.description)
            assertEquals(Type("Thing"), thing?.objectType)
            assertEquals(Context("http://www.w3.org/ns/td"), thing?.objectContext)
            assertEquals(java.util.Map.of("basic_sc", BasicSecurityScheme("header")), thing?.securityDefinitions)
            assertEquals(listOf("basic_sc"), thing?.security)
        }

        @Test
        fun shouldDeserializeGivenJsonToThing2() {
            val json = """{
                          "@context": [
                            "https://www.w3.org/2019/wot/td/v1",
                            {
                              "cov": "http://www.example.org/coap-binding#"
                            },
                            {
                              "saref": "https://w3id.org/saref#"
                            }
                          ],
                          "securityDefinitions": {
                            "noschema": {
                              "scheme": "nosec",
                              "descriptions": {
                                "en": "Basic sec schema"
                              },
                              "description": "Basic sec schema"
                            }
                          },
                          "security": [
                            "noschema"
                          ],
                          "@type": [
                            "saref:LightSwitch"
                          ],
                          "titles": {
                            "en": "English title",
                            "de": "Deutscher Titel"
                          },
                          "title": "English title",
                          "descriptions": {
                            "en": "English description",
                            "de": "Deutsche Beschreibung"
                          },
                          "description": "English description",
                          "properties": {
                            "echo": {
                              "observable": false,
                              "forms": [
                                {
                                  "op": [
                                    "readproperty"
                                  ],
                                  "href": "/echo",
                                  "contentType": "text/plain",
                                  "cov:methodName": "GET"
                                }
                              ]
                            }
                          }
                        }"""
            val thing = Thing.fromJson(json)
            assertEquals("English description", thing?.description)
            assertEquals(Type("saref:LightSwitch"), thing?.objectType)
            assertEquals(
                Context("https://www.w3.org/2019/wot/td/v1").addContext("saref", "https://w3id.org/saref#")
                    .addContext("cov", "http://www.example.org/coap-binding#"), thing?.objectContext
            )
            assertEquals(java.util.Map.of("noschema", NoSecurityScheme()), thing?.securityDefinitions)
            assertEquals(listOf("noschema"), thing?.security)
        }

        @Test
        fun shouldDeserializeGivenJsonToThing3() {
            val json = """{
                          "@context": [
                            "https://www.w3.org/2019/wot/td/v1",
                            {
                              "g": "https://www.w3.org/2019/wot/td/geo/v1"
                            }
                          ],
                          "g:geolocation": {
                             "position": {
                                "longitude": 47.3814,
                                "latitude": -68.323
                             }
                          },
                          "properties": {
                          }
                        }"""
            val thing = Thing.fromJson(json)
            assertEquals(
                java.util.Map.of(
                    "g:geolocation",
                    java.util.Map.of("position", java.util.Map.of("longitude", 47.3814, "latitude", -68.323))
                ), thing?.metadata
            )
        }
    }

    @Test
    @Throws(IOException::class)
    fun fromJsonFile(@TempDir folder: Path) {
        val json =
            "{\"id\":\"Foo\",\"description\":\"Bar\",\"@type\":\"Thing\",\"@context\":[\"http://www.w3.org/ns/td\"]}"
        val file = Paths.get(folder.toString(), "counter.json").toFile()
        Files.writeString(file.toPath(), json)
        val thing = Thing.fromJson(file)
        assertEquals("Foo", thing?.id)
        assertEquals("Bar", thing?.description)
        assertEquals(Type("Thing"), thing?.objectType)
        assertEquals(Context("http://www.w3.org/ns/td"), thing?.objectContext)
    }

    @Test
    fun fromMap() {
        val map: Map<String, *> = mapOf(
            "id" to "Foo",
            "description" to "Bar",
            "@type" to "Thing",
            "@context" to listOf("http://www.w3.org/ns/td")
        )
        val thing = Thing.fromMap(map)
        assertEquals("Foo", thing?.id)
        assertEquals("Bar", thing?.description)
        assertEquals(Type("Thing"), thing?.objectType)
        assertEquals(Context("http://www.w3.org/ns/td"), thing?.objectContext)
    }

    @get:Test
    val propertiesByObjectType: Unit
        get() {
            val json = """{
                          "id" : "KlimabotschafterWetterstationen:Ahrensburg",
                          "title" : "KlimabotschafterWetterstationen:Ahrensburg",
                          "properties" : {
                            "Hum_2m" : {
                              "description" : "Relative Luftfeuchtigkeit 2 m in %",
                              "type" : "number",
                              "observable" : true,
                              "readOnly" : true,
                              "classType" : "java.lang.Object",
                              "@type" : "saref:Humidity"
                            },
                            "Temp_2m" : {
                              "description" : "Temperatur in 2m in Grad Celsisus",
                              "type" : "number",
                              "observable" : true,
                              "readOnly" : true,
                              "classType" : "java.lang.Object",
                              "@type" : "saref:Temperature",
                              "om:unit_of_measure" : "om:degree_Celsius"
                            }
                          },
                          "@type" : "Thing",
                          "@context" : [ "http://www.w3.org/ns/td", {
                            "sane" : "https://sane.city/",
                            "saref" : "https://w3id.org/saref#",
                            "sch" : "http://schema.org/",
                            "om" : "http://www.wurvoc.org/vocabularies/om-1.8/"
                          } ]
                        }"""
            val thing = Thing.fromJson(json)
            assertEquals(1, thing?.getPropertiesByObjectType("saref:Temperature")?.size)
        }

    @get:Test
    val propertiesByExpandedObjectType: Unit
        get() {
            val json = """{
                          "id" : "KlimabotschafterWetterstationen:Ahrensburg",
                          "title" : "KlimabotschafterWetterstationen:Ahrensburg",
                          "properties" : {
                            "Hum_2m" : {
                              "description" : "Relative Luftfeuchtigkeit 2 m in %",
                              "type" : "number",
                              "observable" : true,
                              "readOnly" : true,
                              "classType" : "java.lang.Object",
                              "@type" : "saref:Humidity"
                            },
                            "Temp_2m" : {
                              "description" : "Temperatur in 2m in Grad Celsisus",
                              "type" : "number",
                              "observable" : true,
                              "readOnly" : true,
                              "classType" : "java.lang.Object",
                              "@type" : "saref:Temperature",
                              "om:unit_of_measure" : "om:degree_Celsius"
                            }
                          },
                          "@type" : "Thing",
                          "@context" : [ "http://www.w3.org/ns/td", {
                            "sane" : "https://sane.city/",
                            "saref" : "https://w3id.org/saref#",
                            "sch" : "http://schema.org/",
                            "om" : "http://www.wurvoc.org/vocabularies/om-1.8/"
                          } ]
                        }"""
            val thing = Thing.fromJson(json)
            assertEquals(1, thing?.getPropertiesByExpandedObjectType("https://w3id.org/saref#Temperature")?.size)
        }
}