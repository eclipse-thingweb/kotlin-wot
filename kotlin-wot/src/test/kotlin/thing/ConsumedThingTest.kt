package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.JsonMapper
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.content.JsonCodec
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.thing.action.ThingAction
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.InteractionInput
import ai.ancf.lmos.wot.thing.schema.InteractionListener
import ai.ancf.lmos.wot.thing.schema.StringProperty
import ai.ancf.lmos.wot.thing.schema.StringSchema
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import ai.anfc.lmos.wot.binding.Resource
import ai.anfc.lmos.wot.binding.ResourceType
import com.fasterxml.jackson.databind.node.TextNode
import io.mockk.*
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConsumedThingTest {

    private lateinit var servient: Servient
    private lateinit var protocolClient: ProtocolClient
    private lateinit var protocolClientFactory: ProtocolClientFactory
    private lateinit var consumedThing: ConsumedThing

    @BeforeTest
    fun setUp() {
        protocolClient = mockk()
        protocolClientFactory = mockk()
        every { protocolClientFactory.scheme } returns "https"
        every { protocolClientFactory.client } returns protocolClient
        servient = Servient(clientFactories = listOf(protocolClientFactory))
        val thingDescription = thingDescription {
            title = "Test Thing"
            properties = mutableMapOf(
                "testProperty" to StringProperty(forms = mutableListOf(
                    Form(
                        href = "https://example.com/testProperty",
                        contentType = "application/json",
                        op = listOf(Operation.READ_PROPERTY, Operation.WRITE_PROPERTY, Operation.OBSERVE_PROPERTY, Operation.UNOBSERVE_PROPERTY)
                    )
                ))
            )
            actions = mutableMapOf(
                "testAction" to ThingAction<String, String>("testAction", input = StringSchema(), output = StringSchema(), forms = mutableListOf(
                    Form(
                        href = "https://example.com/testAction",
                        contentType = "application/json",
                        op = listOf(Operation.INVOKE_ACTION)
                    )
                ))
            )
            events = mutableMapOf(
                "testEvent" to ThingEvent<String, String, String> ("testEvent", data = StringSchema(), forms = mutableListOf(
                    Form(
                        href = "https://example.com/testEvent",
                        contentType = "application/json",
                        op = listOf(Operation.SUBSCRIBE_EVENT, Operation.UNSUBSCRIBE_EVENT)
                    )
                ))
            )
        }
        consumedThing = ConsumedThing(servient, thingDescription)
        ContentManager.addCodec(JsonCodec(), true)

    }

    @Test
    fun `test readProperty`() = runBlocking {
        val content = Content("application/json", """{"value": "testValue"}""".toByteArray())
        coEvery { protocolClient.readResource(any<Resource>()) } returns content

        val output = consumedThing.readProperty("testProperty").value()
        assertNotNull(output)
        assertEquals(mutableMapOf("value" to "testValue"), JsonMapper.instance.convertValue(output, Map::class.java))
    }

    @Test
    fun `test readAllProperties`() = runBlocking {
        val content = Content("application/json", """{"value": "testValue"}""".toByteArray())
        coEvery { protocolClient.readResource(any<Resource>()) } returns content

        val properties = consumedThing.readAllProperties()
        assertNotNull(properties)
        assertEquals(1, properties.size)
        val outputValue = properties["testProperty"]?.value()
        assertEquals(mutableMapOf("value" to "testValue"), JsonMapper.instance.convertValue(outputValue, Map::class.java))
    }

    @Test
    fun `test readMultipleProperties`() = runBlocking {
        val content = Content("application/json", """{"value": "testValue"}""".toByteArray())
        coEvery { protocolClient.readResource(any<Resource>()) } returns content

        val properties = consumedThing.readMultipleProperties(listOf("testProperty"))
        assertNotNull(properties)
        assertEquals(1, properties.size)
        val outputValue = properties["testProperty"]?.value()
        assertEquals(mutableMapOf("value" to "testValue"), JsonMapper.instance.convertValue(outputValue, Map::class.java))
    }

    @Test
    fun `test writeProperty`() = runBlocking {
        val value = InteractionInput.Value(TextNode("newValue"))
        coJustRun { protocolClient.writeResource(any<Resource>(), any<Content>()) }

        consumedThing.writeProperty("testProperty", value)
        coVerify { protocolClient.writeResource(any<Resource>(), any<Content>()) }
    }

    @Test
    fun `test writeMultipleProperties`() = runBlocking {
        val valueMap = mapOf(
            "testProperty" to TextNode("newValue")
        )
        coJustRun { protocolClient.writeResource(any<Resource>(), any<Content>()) }

        consumedThing.writeMultipleProperties(valueMap)
        coVerify(exactly = 1) { protocolClient.writeResource(any<Resource>(), any<Content>()) }
    }

    @Test
    fun `test invokeAction`() = runBlocking {
        val content = Content("application/json", """{"value": "testValue"}""".toByteArray())
        coEvery { protocolClient.invokeResource(any<Resource>(), any<Content>()) } returns content

        val params = InteractionInput.Value(TextNode("actionInput"))
        val output = consumedThing.invokeAction("testAction", params)
        assertNotNull(output)
        val outputValue = output.value()
        assertEquals(mutableMapOf("value" to "testValue"), JsonMapper.instance.convertValue(outputValue, Map::class.java))
    }

    @Test
    fun `test observeProperty`(): Unit = runBlocking {
        val listener = mockk<InteractionListener>(relaxed = true)
        coEvery { protocolClient.subscribeResource(any<Resource>(), any<ResourceType>()) } returns emptyFlow()

        val subscription = consumedThing.observeProperty("testProperty", listener)
        assertEquals(true, subscription.active)
    }

    @Test
    fun `test subscribeEvent`(): Unit = runBlocking {
        val listener = mockk<InteractionListener>(relaxed = true)
        coEvery { protocolClient.subscribeResource(any<Resource>(), any<ResourceType>()) } returns emptyFlow()

        val subscription = consumedThing.subscribeEvent("testEvent", listener)
        assertNotNull(subscription)
        assertEquals(true, subscription.active)
    }

    @Test
    fun testEquals() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ConsumedThing(Servient(), thingDescription)
        val thingB = ConsumedThing(Servient(), thingDescription)
        assertEquals(thingA, thingB)
    }

    @Test
    fun testHashCode() {
        val thingDescription = ThingDescription(
            title = "foo",
            objectType = Type("Thing"),
            objectContext = Context("http://www.w3.org/ns/td")
        )

        val thingA = ConsumedThing(Servient(), thingDescription).hashCode()
        val thingB = ConsumedThing(Servient(), thingDescription).hashCode()
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
        val thing = ConsumedThing.fromJson(json)
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
