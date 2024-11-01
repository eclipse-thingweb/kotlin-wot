package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.thing.schema.StringSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class ThingEventTest {

    @Test
    fun testThingEvent() {
        // Assuming ThingEvent can be directly constructed instead of using Builder
        val event = ThingEvent(
            data = StringSchema()
        )
        assertEquals("string", event.data?.type)
    }

    @Test
    fun testEquals() {
        val eventA = ThingEvent(
            data = StringSchema()
        )
        val eventB = ThingEvent(
            data = StringSchema()
        )
        assertEquals(eventA, eventB)
    }

    @Test
    fun testHashCode() {
        val eventA = ThingEvent(
            data = StringSchema()
        ).hashCode()
        val eventB = ThingEvent(
            data = StringSchema()
        ).hashCode()
        assertEquals(eventA, eventB)
    }

}