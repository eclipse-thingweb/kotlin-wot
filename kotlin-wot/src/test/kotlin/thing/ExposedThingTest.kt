package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.content.JsonCodec
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.*
import com.fasterxml.jackson.databind.node.TextNode
import io.mockk.*
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import net.javacrumbs.jsonunit.core.Option
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExposedThingTest {

    @BeforeTest
    fun setUp() {

        ContentManager.addCodec(JsonCodec(), true)

    }

    @Test
    fun testEquals() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ExposedThing(Servient(), thingDescription)
        val thingB = ExposedThing(Servient(), thingDescription)
        assertEquals(thingA, thingB)
    }

    @Test
    fun testHashCode() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ExposedThing(Servient(), thingDescription).hashCode()
        val thingB = ExposedThing(Servient(), thingDescription).hashCode()
        assertEquals(thingA, thingB)
    }

    @Test
    fun toJson() {
        val thingDescription = ThingDescription(
            id = "foo",
            title = "foo",
            description = "Bla bla",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)

        val thingAsJson = JsonMapper.instance.writeValueAsString(exposedThing)
        JsonAssertions.assertThatJson(thingAsJson)
            .`when`(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(
                """{
                    "id": "foo",
                    "title":"foo",
                    "description":"Bla bla",
                    "@type":"Thing",
                    "@context":"http://www.w3.org/ns/td"
                }"""
            )
    }

    @Test
    fun shouldDeserializeGivenJsonToThing() {
        val json = """{
                    "id": "Foo",
                    "@type": "Thing",
                    "@context": "https://www.w3.org/2022/wot/td/v1.1",
                    "title": "Test Thing",
                    "description": "A test thing for unit testing",
                    "properties": {
                        "stringProperty": {
                            "type": "string",
                            "title": "propertyTitle",
                            "enum": [
                                "a",
                                "b",
                                "c"
                            ],
                            "forms": [
                                {
                                    "href": "https://example.com/stringProperty",
                                    "contentType": "application/json",
                                    "op": ["readproperty", "writeproperty"]
                                }
                            ]
                        }
                    },
                    "actions": {
                        "action": {
                            "title": "actionTitle",
                            "input": {
                                "type": "string",
                                "default": "test",
                                "minLength": 10
                            },
                            "output": {
                                "type": "integer"
                            },
                            "forms": [
                                {
                                    "href": "https://example.com/action",
                                    "contentType": "application/json",
                                    "op": ["invokeaction"]
                                }
                            ]
                        }
                    },
                    "events": {
                        "event": {
                            "title": "eventTitle",
                            "data": {
                                "type": "string"
                            },
                            "forms": [
                                {
                                    "href": "https://example.com/event",
                                    "contentType": "application/json",
                                    "op": ["subscribeevent", "unsubscribeevent"]
                                }
                            ]
                        }
                    }
                }"""
        val thing = ExposedThing.fromJson(json)
        assertNotNull(thing)
        assertEquals("Foo", thing.id)
        assertEquals("Test Thing", thing.title)
        assertEquals("A test thing for unit testing", thing.description)
        assertEquals(Type("Thing"), thing.objectType)
        assertEquals(Context("https://www.w3.org/2022/wot/td/v1.1"), thing.objectContext)
        assertNotNull(thing.properties["stringProperty"])
        assertNotNull(thing.actions["action"])
        assertNotNull(thing.events["event"])
    }

    @Test
    fun `test handleReadProperty`() = runBlocking {
        val property = StringProperty(forms = mutableListOf(
            Form(
                href = "https://example.com/testProperty",
                contentType = "application/json",
                op = listOf(Operation.READ_PROPERTY)
            )
        ))
        val thingDescription = ThingDescription(
            title = "Test Thing",
            properties = mutableMapOf("testProperty" to property)
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)
        exposedThing.setPropertyReadHandler("testProperty") {
            mutableMapOf("value" to "testValue").toInteractionInputValue()
        }

        val content = exposedThing.handleReadProperty("testProperty")
        assertNotNull(content)
        assertEquals("application/json", content.type)
        assertEquals("""{"value":"testValue"}""", String(content.body))
    }

    @Test
    fun `test handleWriteProperty`() = runBlocking {
        val property = StringProperty(forms = mutableListOf(
            Form(
                href = "https://example.com/testProperty",
                contentType = "application/json",
                op = listOf(Operation.WRITE_PROPERTY)
            )
        ))
        val thingDescription = ThingDescription(
            title = "Test Thing",
            properties = mutableMapOf("testProperty" to property)
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)
        exposedThing.setPropertyWriteHandler("testProperty") { input, options ->
            mutableMapOf("value" to "testValue").toInteractionInputValue()
        }

        val content = exposedThing.handleWriteProperty("testProperty", Content("application/json", """{"value": "newValue"}""".toByteArray()))
        assertNotNull(content)
        assertEquals("application/json", content.type)
        assertEquals("""{"value":"testValue"}""", String(content.body))
    }

    @Test
    fun `test handleInvokeAction`() = runBlocking {
        val action = ThingAction<String, String>(title = "testAction",  forms = mutableListOf(
            Form(
                href = "https://example.com/testAction",
                contentType = "application/json",
                op = listOf(Operation.INVOKE_ACTION)
            )
        ))
        val thingDescription = ThingDescription(
            title = "Test Thing",
            actions = mutableMapOf("testAction" to action)
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)
        exposedThing.setActionHandler("testAction") { input , options ->
            mutableMapOf("value" to "testValue").toInteractionInputValue()
        }

        val content = exposedThing.handleInvokeAction("testAction", Content("application/json", """{"input": "actionInput"}""".toByteArray()))
        assertNotNull(content)
        assertEquals("application/json", content.type)
        assertEquals("{\"value\":\"testValue\"}", String(content.body))
    }

    @Test
    fun `test handleReadAllProperties`() = runBlocking {
        val property1 = StringProperty(forms = mutableListOf(
            Form(
                href = "https://example.com/property1",
                contentType = "application/json",
                op = listOf(Operation.READ_PROPERTY)
            )
        ))
        val property2 = StringProperty(forms = mutableListOf(
            Form(
                href = "https://example.com/property2",
                contentType = "application/json",
                op = listOf(Operation.READ_PROPERTY)
            )
        ))
        val thingDescription = ThingDescription(
            title = "Test Thing",
            properties = mutableMapOf("property1" to property1, "property2" to property2)
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)
        exposedThing.setPropertyReadHandler("property1") {
            mutableMapOf("value" to "testValue1").toInteractionInputValue()
        }
        exposedThing.setPropertyReadHandler("property2") {
            mutableMapOf("value" to "testValue2").toInteractionInputValue()
        }

        val properties = exposedThing.handleReadAllProperties()
        assertNotNull(properties)
        assertEquals(2, properties.size)
        assertEquals("""{"value":"testValue1"}""", String(properties["property1"]!!.body))
        assertEquals("""{"value":"testValue2"}""", String(properties["property2"]!!.body))
    }

    @Test
    fun `test handleReadMultipleProperties`() = runBlocking {
        val property1 = StringProperty(forms = mutableListOf(
            Form(
                href = "https://example.com/property1",
                contentType = "application/json",
                op = listOf(Operation.READ_PROPERTY)
            )
        ))
        val property2 = StringProperty(forms = mutableListOf(
            Form(
                href = "https://example.com/property2",
                contentType = "application/json",
                op = listOf(Operation.READ_PROPERTY)
            )
        ))
        val thingDescription = ThingDescription(
            title = "Test Thing",
            properties = mutableMapOf("property1" to property1, "property2" to property2)
        )
        val exposedThing = ExposedThing(Servient(), thingDescription)
        exposedThing.setPropertyReadHandler("property1") {
            mutableMapOf("value" to "testValue1").toInteractionInputValue()
        }
        exposedThing.setPropertyReadHandler("property2") {
            mutableMapOf("value" to "testValue2").toInteractionInputValue()
        }

        val properties = exposedThing.handleReadMultipleProperties(listOf("property1", "property2"))
        assertNotNull(properties)
        assertEquals(2, properties.size)
        assertEquals("""{"value":"testValue1"}""", String(properties["property1"]!!.body))
        assertEquals("""{"value":"testValue2"}""", String(properties["property2"]!!.body))
    }


    @Test
    fun `test handleObserveProperty and emitPropertyChange`() = runBlocking {
        val thingDescription = thingDescription {
            title = "Test Thing"
            stringProperty("testProperty") {
                forms = mutableListOf(
                    Form(
                        href = "https://example.com/testProperty",
                        contentType = "application/json",
                        op = listOf(Operation.READ_PROPERTY, Operation.OBSERVE_PROPERTY)
                    )
                )
                observable = true
            }
        }
        val exposedThing = ExposedThing(Servient(), thingDescription)

        val data = InteractionInput.Value(TextNode("propertyChangeData"))
        val content = Content("application/json", "\"propertyChangeData\"".toByteArray())
        val contentListener = mockk<ContentListener>()
        val propertyReadHandler = mockk<PropertyReadHandler>()

        coEvery { propertyReadHandler.handle(any()) } returns data
        coEvery { contentListener.handle(any<Content>()) } just runs

        exposedThing.setPropertyObserveHandler("testProperty", propertyReadHandler)
        exposedThing.handleObserveProperty(propertyName = "testProperty", listener = contentListener)

        exposedThing.emitPropertyChange("testProperty", data)

        coVerify { propertyReadHandler.handle(any()) }
        coVerify { contentListener.handle(any<Content>()) }

        exposedThing.setPropertyUnobserveHandler("testProperty", propertyReadHandler)
        exposedThing.handleUnobserveProperty(propertyName = "testProperty")

        coVerify { propertyReadHandler.handle(any()) }
        coVerify { contentListener.handle(any<Content>()) }
    }

    @Test
    fun `test handleSubscribeEvent and emitEvent`() = runBlocking {
        val thingDescription = thingDescription {
            title = "Test Thing"
            event<String, String, String>("testEvent") {
                data = StringSchema()
                forms = mutableListOf(
                    Form(
                        href = "https://example.com/testEvent",
                        contentType = "application/json",
                        op = listOf(Operation.SUBSCRIBE_EVENT, Operation.UNSUBSCRIBE_EVENT)
                    )
                )
            }
        }

        val exposedThing = ExposedThing(Servient(), thingDescription)

        val data = InteractionInput.Value(TextNode("eventData"))
        val content = Content("application/json", "\"eventData\"".toByteArray())
        val eventSubscriptionHandler = mockk<EventSubscriptionHandler>()
        val contentListener = mockk<ContentListener>()

        coEvery { eventSubscriptionHandler.handle(any()) } just runs
        coEvery { contentListener.handle(any<Content>()) } just runs

        exposedThing.setEventSubscribeHandler("testEvent", eventSubscriptionHandler)
        exposedThing.handleSubscribeEvent(eventName = "testEvent", listener = contentListener)

        exposedThing.emitEvent("testEvent", data)
        coVerify { eventSubscriptionHandler.handle(any()) }
        coVerify { contentListener.handle(any<Content>()) }

        exposedThing.setEventUnsubscribeHandler("testEvent", eventSubscriptionHandler)
        exposedThing.handleUnsubscribeEvent(eventName = "testEvent")

        coVerify { eventSubscriptionHandler.handle(any()) }
        coVerify { contentListener.handle(any<Content>()) }
    }



}