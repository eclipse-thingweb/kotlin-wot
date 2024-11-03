package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.StringSchema
import com.fasterxml.jackson.module.kotlin.readValue
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.Test
import kotlin.test.assertEquals

class ThingEventTest {

    @Test
    fun testThingEvent() {
        // Assuming ThingEvent can be directly constructed instead of using Builder
        val event = ThingEvent<String, String, String>(
            data = StringSchema(title = "title")
        )
        assertEquals("title", event.data?.title)
    }


    @Test
    fun testToJson() {
        val event = ThingEvent<String, String, String>(
            title = "event",
            data = StringSchema()
        )
        val json = JsonMapper.instance.writeValueAsString(event)

        JsonAssertions.assertThatJson(json)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                    "title": "event", 
                    "data":
                        {"type":"string"}
                    }
                """
            )
    }

    @Test
    fun fromJson() {
        val json = """{
                    "title": "event", 
                    "data":
                        {"type":"string"}
                    }
                """

        val parsedEvent = JsonMapper.instance.readValue<ThingEvent<String, String, String>>(json)
        val event = ThingEvent<String, String, String>(
            title = "event",
            data = StringSchema())
        assertEquals(event, parsedEvent)
    }

    @Test
    fun testEquals() {
        val data = StringSchema()

        val eventA = ThingEvent<String, String, String>(
            data = data
        )
        val eventB = ThingEvent<String, String, String>(
            data = data
        )
        assertEquals(eventA, eventB)
    }

    @Test
    fun testHashCode() {
        val data = StringSchema()

        val eventA = ThingEvent<String, String, String>(
            data = data
        ).hashCode()
        val eventB = ThingEvent<String, String, String>(
            data = data
        ).hashCode()
        assertEquals(eventA, eventB)
    }

}