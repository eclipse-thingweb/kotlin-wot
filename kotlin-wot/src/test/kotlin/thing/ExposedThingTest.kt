package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.BasicSecurityScheme
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedThingTest {


    @Test
    fun testEquals() {
        val thing = Thing(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ExposedThing.from(thing)
        val thingB = ExposedThing.from(thing)
        assertEquals(thingA, thingB)
    }

    @Test
    fun testHashCode() {
        val thing = Thing(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ExposedThing.from(thing).hashCode()
        val thingB = ExposedThing.from(thing).hashCode()
        assertEquals(thingA, thingB)
    }


    @Test
    fun toJson() {
        val thing = Thing(
            id  = "foo",
            title = "foo",
            description = "Bla bla",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )
        val exposedThing = ExposedThing.from(thing)

        val thingAsJson = exposedThing.toJson()
        println(thingAsJson)
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
