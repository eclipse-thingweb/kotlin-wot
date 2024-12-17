package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.ancf.lmos.wot.thing.schema.StringProperty
import ai.ancf.lmos.wot.thing.schema.toInteractionInputValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionAwareProtocolListenerRegistryTest {

    @Test
    fun registerAddsListenerForSpecificSessionSuccessfully() {
        val registry = SessionAwareProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        registry.register("session1", affordance, 0, listener)
        assertNotNull(
            registry.registryMap["session1"]?.listeners?.get(affordance)?.get(0)
        )
    }

    @Test
    fun notifyCallsListenersAcrossAllSessions() = runTest {
        val registry = SessionAwareProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener1 = mockk<ContentListener>(relaxed = true)
        val listener2 = mockk<ContentListener>(relaxed = true)

        registry.register("session1", affordance, 0, listener1)
        registry.register("session2", affordance, 0, listener2)
        registry.notify(affordance, "testContent".toInteractionInputValue(), affordance, 0)

        coEvery { listener1.handle(any()) }
        coEvery { listener2.handle(any()) }
    }

    @Test
    fun unregisterListenerForSpecificSession() {
        val registry = SessionAwareProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        registry.register("session1", affordance, 0, listener)
        registry.unregister("session1", affordance, 0)
        assertNull(
            registry.registryMap["session1"]?.listeners?.get(affordance)?.get(0)
        )
    }

    @Test
    fun unregisterAllListenersForSpecificSession() {
        val registry = SessionAwareProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener1 = ContentListener { content -> println(content) }
        val listener2 = ContentListener { content -> println(content) }

        registry.register("session1", affordance, 0, listener1)
        registry.register("session1", affordance, 0, listener2)
        registry.unregisterAll("session1")
        assertTrue(registry.registryMap["session1"]?.listeners == null)
    }

    @Test
    fun unregisterAllSessionsClearsAllListeners() {
        val registry = SessionAwareProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        registry.register("session1", affordance, 0, listener)
        registry.register("session2", affordance, 0, listener)
        registry.unregisterAllSessions()
        assertTrue(registry.registryMap.isEmpty())
    }

    @Test
    fun notifyHandlesFormIndexNullAcrossSessions() = runTest {
        val registry = SessionAwareProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener1 = mockk<ContentListener>(relaxed = true)
        val listener2 = mockk<ContentListener>(relaxed = true)

        registry.register("session1", affordance, 0, listener1)
        registry.register("session2", affordance, 0, listener2)
        registry.notify(affordance, "testContent".toInteractionInputValue(), affordance)

        coEvery { listener1.handle(any()) }
        coEvery { listener2.handle(any()) }
    }
}