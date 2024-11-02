package ai.ancf.lmos.wot.thing.event


import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.thing.schema.StringSchema
import app.cash.turbine.test
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedThingEventTest {
    private var name: String? = null
    private var state: ExposedThingEvent.EventState<*>? = null

    @BeforeTest
    fun setUp() {
        name = "change"
        state = mockk()
    }

    @Test
    fun testEquals() {
        val eventA = ExposedThingEvent<String, String, String>(ThingEvent(
            data = StringSchema()
        ))
        val eventB = ExposedThingEvent<String, String, String>(ThingEvent(
            data = StringSchema()
        ))
        assertEquals(eventA, eventB)
    }

    @Test
    fun testHashCode() {
        val eventA = ExposedThingEvent<String, String, String>(ThingEvent(
            data = StringSchema()
        )).hashCode()
        val eventB = ExposedThingEvent<String, String, String>(ThingEvent(
            data = StringSchema()
        )).hashCode()
        assertEquals(eventA, eventB)
    }
    @Test
    fun testToJson() {
        val event = ThingEvent<String, String, String>(
            title = "event",
            data = StringSchema()
        )
        val exposedThingEvent = ExposedThingEvent(event)
        val json = JsonMapper.instance.writeValueAsString(exposedThingEvent)

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
                    }"""

        val parsedThingEvent = JsonMapper.instance.readValue<ExposedThingEvent<String, String, String>>(json)
        val event = ThingEvent<String, String, String>(
            title = "event",
            data = StringSchema()
        )
        val exposedThingEvent = ExposedThingEvent(event)
        assertEquals(exposedThingEvent, parsedThingEvent)
    }


    @Test
    fun emitWithoutDataShouldEmitNullAsNextValueToEventState(): Unit = runTest {
        val event = ThingEvent<String, String, String>(data = StringSchema())
        val exposedThingEvent = ExposedThingEvent(event)

        // Collect emissions and check expected value
        exposedThingEvent.getState().flow.test {
            exposedThingEvent.emit("Event")
            assertEquals("Event", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}