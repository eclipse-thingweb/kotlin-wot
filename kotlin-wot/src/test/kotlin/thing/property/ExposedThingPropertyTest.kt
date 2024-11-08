package ai.ancf.lmos.wot.thing.property

/*
class ExposedThingPropertyTest {

    private lateinit var thing: ExposedThingImpl
    private lateinit var state: PropertyState<String>

    @BeforeTest
    fun setUp() {
        thing = ExposedThingImpl(id = "testThing")
        state = mockk()


    }

    @Test
    fun testEquals() {
        val property1 = ExposedStringProperty(thing = thing, property = stringProperty { title = "title" })
        val property2 = ExposedStringProperty(thing = thing, property = stringProperty { title = "title" })
        assertEquals(property1, property2)
    }

    @Test
    fun testHashCode() {
        val thing = ExposedThingImpl(id = "Foo")

        val property1 = ExposedStringProperty(thing = thing, property = stringProperty { title = "title" }).hashCode()
        val property2 = ExposedStringProperty(thing = thing, property = stringProperty { title = "title" }).hashCode()
        assertEquals(property1, property2)
    }

    @Test
    fun toJson() {
        val property = exposedIntProperty {
            objectType=Type("saref:Temperature")
            description = "bla bla"
            observable=true
            readOnly=true
        }

        JsonAssertions.assertThatJson(JsonMapper.instance.writeValueAsString(property))
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{"@type":"saref:Temperature","description":"bla bla","type":"integer","observable":true,"readOnly":true}""")
    }

    @Test
    fun fromJson() {
        val json = """{"@type":"saref:Temperature","description":"bla bla","type":"integer","observable":true,"readOnly":true}"""

        val parsedProperty = JsonMapper.instance.readValue<ExposedIntProperty>(json)
        assertEquals(Type("saref:Temperature"), parsedProperty.objectType)
        assertEquals("bla bla", parsedProperty.description)
        assertEquals(true, parsedProperty.observable)
        assertEquals(true, parsedProperty.readOnly)
    }

    @Test
    fun readWithoutHandlerShouldReturnStateValue() = runTest {
        every { state.readHandler } returns null
        coEvery { state.value } returns "test"

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.read()

        assertEquals("test", value)
    }

    @Test
    fun readWithHandlerShouldCallHandler() = runTest {
        val readHandler: suspend () -> String? = mockk()

        every { state.readHandler } returns readHandler
        coEvery { readHandler() } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.read()

        assertEquals("test", value)
    }


    @Test
    fun readWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val readHandler: suspend () -> String? = mockk()

        coEvery { readHandler() } throws IOException()
        every { state.readHandler } returns readHandler

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        assertFailsWith<IOException> { exposedProperty.read() }
    }

    @Test
    fun writeWithoutHandlerShouldSetStateValue() = runTest{
        val writeHandler: suspend (String) -> String? = mockk()

        every { state.writeHandler } returns null
        coEvery { writeHandler(any()) } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.write("test")

        assertEquals("test", value)
    }

    @Test
    fun writeWithHandlerShouldCallHandler() = runTest{
        val writeHandler: suspend (String) -> String? = mockk()

        every { state.writeHandler } returns writeHandler
        coEvery { writeHandler(any()) } returns "test"
        coEvery { state.setValue(any()) } just Runs

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        val value = exposedProperty.write("test")

        assertEquals("test", value)
    }

    @Test
    fun writeWithBrokenHandlerShouldThrowException(): Unit = runTest {
        val writeHandler: suspend (String) -> String? = mockk()

        coEvery { writeHandler(any()) } throws IOException()
        every { state.writeHandler } returns writeHandler

        val exposedProperty = ExposedStringProperty(thing = thing, state = state)

        assertFailsWith<IOException> { exposedProperty.write("test") }
    }

}
 */