package ai.ancf.lmos.wot.thing

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedThingTest {

    @Test
    fun testEquals() {
        val thingA = ExposedThing(id = "id")
        val thingB = ExposedThing(id = "id")
        assertEquals(thingA, thingB)
    }

    @Test
    fun toJson() {
        val thing = Thing(
            id = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )
        val exposedThing = ExposedThing(thing)
        //val jsonString = Json.encodeToString(thing)
        //println(jsonString)

        val thingAsJson = exposedThing.toJson()
        JsonAssertions.assertThatJson(thingAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{    
                    "id":"foo",
                    "@type":"Thing",
                    "@context":"http://www.w3.org/ns/td"
                }"""
            )
    }
}
