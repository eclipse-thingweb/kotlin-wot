package ai.ancf.lmos.wot.reflection

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.content.toJsonContent
import ai.ancf.lmos.wot.reflection.things.SimpleThing
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import ai.ancf.lmos.wot.thing.schema.StringSchema
import kotlinx.coroutines.test.runTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SimpleThingTest {

    lateinit var servient: Servient
    lateinit var wot: Wot
    lateinit var simpleThing: SimpleThing
    lateinit var exposedThing: ExposedThing

    @BeforeTest
    fun setUp() = runTest {
        // Set up the servient and WoT instance
        val httpProtocolServer = HttpProtocolServer()
        httpProtocolServer.started = true
        servient = Servient(servers = listOf(httpProtocolServer))
        wot = Wot.create(servient)

        // Create an instance of ComplexThing
        simpleThing = SimpleThing()

        // Generate ThingDescription from the class
        exposedThing = ExposedThingBuilder.createExposedThing(wot, simpleThing, SimpleThing::class) as ExposedThing

        servient.addThing(exposedThing)
        servient.expose("simpleThing")
    }

    @Test
    fun `Read mutable property`() = runTest {
        val content = exposedThing.handleReadProperty("mutableProperty")
        val response = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("test", response.value)
    }

    @Test
    fun `Read observable property`() = runTest {
        val content = exposedThing.handleReadProperty("observableProperty")
        val response = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("Hello World", response.value)
        simpleThing.changeObservableProperty()
        val updatedContent = exposedThing.handleReadProperty("observableProperty")
        val updatedResponse = ContentManager.contentToValue(updatedContent, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("Hello from action!", updatedResponse.value)
    }

    @Test
    fun `Get observable property update`() = runTest {
        var lock = CountDownLatch(1);
        val contentListener: ContentListener = (ContentListener { lock.countDown() })

        exposedThing.handleObserveProperty(propertyName = "observableProperty", listener = contentListener)

        lock.await(2000, TimeUnit.MILLISECONDS);

        lock = CountDownLatch(1);

        simpleThing.changeObservableProperty()

        lock.await(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    fun `Write writeOnly property`() = runTest {
        val content = exposedThing.handleWriteProperty("writeOnlyProperty", "newValue".toJsonContent())
        val response = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("newValue", response.value)
        assertEquals("newValue", simpleThing.writeOnlyProperty)
    }

    @Test
    fun `Write mutable property`() = runTest {
        val content = exposedThing.handleWriteProperty("mutableProperty", "newValue".toJsonContent())
        val response = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("newValue", response.value)
        assertEquals("newValue", simpleThing.mutableProperty)
    }

    @Test
    fun `Write readonly property`() = runTest {
        assertFailsWith<IllegalArgumentException>("ExposedThing 'Simple Thing' has no writerHandler for Property 'readyOnlyProperty'") {
            exposedThing.handleWriteProperty("readyOnlyProperty", "newValue".toJsonContent())
        }
    }

    @Test
    fun `Read writeOnly property`() = runTest {
        assertFailsWith<IllegalArgumentException>("ExposedThing 'Simple Thing' has no readHandler for Property 'writeOnlyProperty'") {
            exposedThing.handleReadProperty("writeOnlyProperty")
        }
    }

    @Test
    fun `Invoke voidAction`() = runTest {
        exposedThing.handleInvokeAction("voidAction")
        assertEquals(1, simpleThing.counter)
    }

    @Test
    fun `Invoke inputAction`() = runTest {
        exposedThing.handleInvokeAction("inputAction", "test".toJsonContent())
        assertEquals(1, simpleThing.counter)
    }

    @Test
    fun `Invoke outputAction`() = runTest {
        val content = exposedThing.handleInvokeAction("outputAction")
        val response = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("test", response.value)
    }

    @Test
    fun `Invoke inOutAction`() = runTest {
        val content = exposedThing.handleInvokeAction("inOutAction", "test".toJsonContent())
        val response = ContentManager.contentToValue(content, StringSchema()) as DataSchemaValue.StringValue
        assertEquals("test output", response.value)
    }

    @Test
    fun `Subscribe to statusUpdated event `() = runTest {
        var lock = CountDownLatch(1);
        val contentListener: ContentListener = (ContentListener { lock.countDown() })

        exposedThing.handleSubscribeEvent(eventName = "statusUpdated", listener = contentListener)

        lock.await(2000, TimeUnit.MILLISECONDS);
    }
}