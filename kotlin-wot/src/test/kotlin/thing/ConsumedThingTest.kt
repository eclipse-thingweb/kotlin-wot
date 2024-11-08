package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsumedThingTest {


    @Test
    fun testEquals() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ConsumedThing(Servient(), thingDescription)
        val thingB = ConsumedThing(Servient(), thingDescription)
        assertEquals(thingA, thingB)
    }

    @Test
    fun testHashCode() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ConsumedThing(Servient(), thingDescription).hashCode()
        val thingB = ConsumedThing(Servient(), thingDescription).hashCode()
        assertEquals(thingA, thingB)
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
        val thing = ConsumedThing.fromJson(json)
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
