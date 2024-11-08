package ai.ancf.lmos.wot.thing.event


/*
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
        val data = StringSchema()

        val eventA = ExposedThingEvent<String, String, String>(ThingEvent(
            data = data
        ))
        val eventB = ExposedThingEvent<String, String, String>(ThingEvent(
            data = data
        ))
        assertEquals(eventA, eventB)
    }

    @Test
    fun testHashCode() {
        val data = StringSchema()

        val eventA = ExposedThingEvent<String, String, String>(ThingEvent(
            data = data
        )).hashCode()
        val eventB = ExposedThingEvent<String, String, String>(ThingEvent(
            data = data
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
*/