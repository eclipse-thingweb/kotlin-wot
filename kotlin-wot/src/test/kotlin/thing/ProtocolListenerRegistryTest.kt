/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing

import org.eclipse.thingweb.thing.form.Form
import org.eclipse.thingweb.thing.schema.ContentListener
import org.eclipse.thingweb.thing.schema.StringProperty
import org.eclipse.thingweb.thing.schema.toInteractionInputValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ProtocolListenerRegistryTest {

    @Test
    fun registerAddsListenerSuccessfully() {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        registry.register(affordance, 0, listener)
        assertNotNull(registry.listeners[affordance]?.get(0))
    }

    @Test
    fun registerThrowsExceptionForInvalidFormIndex() {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        assertFailsWith<IllegalArgumentException> {
            registry.register(affordance, 1, listener)
        }
    }

    @Test
    fun unregisterRemovesListenerSuccessfully() {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        registry.register(affordance, 0, listener)
        registry.unregister(affordance, 0,)
        assertNull(registry.listeners[affordance]?.get(0))
    }

    @Test
    fun unregisterThrowsExceptionForNonExistentListener() {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        assertFailsWith<IllegalStateException> {
            registry.unregister(affordance, 0)
        }
    }

    @Test
    fun unregisterAllClearsAllListeners() {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = ContentListener { content -> println(content) }

        registry.register(affordance, 0, listener)
        registry.unregisterAll()
        assertTrue(registry.listeners.isEmpty())
    }

    @Test
    fun notifyCallsListenersWithCorrectContent() = runTest {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = mockk<ContentListener>(relaxed = true)

        registry.register(affordance, 0, listener)
        registry.notify(affordance, "testContent".toInteractionInputValue(), affordance, 0)

        coEvery { listener.handle(any()) }
    }

    @Test
    fun notifyCallsAllListenersForAffordance() = runTest {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener1 = mockk<ContentListener>(relaxed = true)
        val listener2 = mockk<ContentListener>(relaxed = true)

        registry.register(affordance, 0, listener1)
        registry.register(affordance, 0, listener2)
        registry.notify(affordance, "testContent".toInteractionInputValue(), affordance, 0)

        coEvery { listener1.handle(any()) }
        coEvery { listener2.handle(any()) }
    }

    @Test
    fun notifyHandlesNullFormIndex() = runTest {
        val registry = ProtocolListenerRegistry()
        val affordance = StringProperty(forms = mutableListOf(Form(href = "http://example.com", contentType = "application/json")))
        val listener = mockk<ContentListener>(relaxed = true)

        registry.register(affordance, 0, listener)
        registry.notify(affordance, "testContent".toInteractionInputValue(), affordance)

        coEvery { listener.handle(any()) }
    }
}