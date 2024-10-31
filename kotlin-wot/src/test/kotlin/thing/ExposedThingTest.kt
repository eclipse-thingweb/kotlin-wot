package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
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

        val thingA = ExposedThing(thing)
        val thingB = ExposedThing(thing)
        assertEquals(thingA, thingB)
    }

    @Test
    fun toJson() {
        val thing = Thing(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )
        val exposedThing = ExposedThing(thing)

        val thingAsJson = JsonMapper.instance.writeValueAsString(exposedThing)
        JsonAssertions.assertThatJson(thingAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{    
                    "title":"foo",
                    "@type":"Thing",
                    "@context":"http://www.w3.org/ns/td"
                }"""
            )
    }
}
