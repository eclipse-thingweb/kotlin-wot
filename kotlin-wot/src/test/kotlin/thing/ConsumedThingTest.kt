package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsumedThingTest {


    @Test
    fun testEquals() {
        val thing = Thing(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ConsumedThingImpl(Servient(), thing)
        val thingB = ConsumedThingImpl(Servient(), thing)
        assertEquals(thingA, thingB)
    }

    @Test
    fun testHashCode() {
        val thing = Thing(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ConsumedThingImpl(Servient(), thing).hashCode()
        val thingB = ConsumedThingImpl(Servient(), thing).hashCode()
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
        val thing = ConsumedThingImpl.fromJson(json)
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
