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
}