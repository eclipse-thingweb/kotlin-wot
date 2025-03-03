/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing

import org.eclipse.thingweb.thing.schema.ContentListener
import org.eclipse.thingweb.thing.schema.DataSchema
import org.eclipse.thingweb.thing.schema.InteractionAffordance
import org.eclipse.thingweb.thing.schema.InteractionInput
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * A session-aware wrapper for the `ProtocolListenerRegistry`.
 *
 * This class extends the functionality of `ProtocolListenerRegistry` by providing a simplified
 * interface for managing listeners across multiple sessions.
 *
 * Internally, it uses an instance of `ProtocolListenerRegistry` to delegate the management
 * of listeners and affordances.
 */
class SessionAwareProtocolListenerRegistry {

    internal val registryMap: ConcurrentMap<String, ProtocolListenerRegistry> = ConcurrentHashMap()

    /**
     * Registers a listener for a specific session, affordance, and form index.
     *
     * @param sessionId The unique ID of the session.
     * @param affordance The interaction affordance for which the listener is being registered.
     * @param formIndex The index of the form associated with the affordance.
     * @param listener The listener to register.
     *
     * Delegates to `ProtocolListenerRegistry.register`.
     */
    fun register(sessionId: String, affordance: InteractionAffordance, formIndex: Int, listener: ContentListener) {
        val registry = registryMap.getOrPut(sessionId) { ProtocolListenerRegistry() }
        registry.register(affordance, formIndex, listener)
    }

    /**
     * Unregisters a listener for a specific session, affordance, and form index.
     *
     * @param sessionId The unique ID of the session.
     * @param affordance The interaction affordance from which the listener is being removed.
     * @param formIndex The index of the form associated with the affordance.
     *
     * Delegates to `ProtocolListenerRegistry.unregister`.
     */
    fun unregister(sessionId: String, affordance: InteractionAffordance, formIndex: Int) {
        val registry = registryMap[sessionId] ?: throw IllegalStateException("Session not found")
        registry.unregister(affordance, formIndex)
    }

    /**
     * Unregisters all listeners associated with a specific session.
     *
     * @param sessionId The unique ID of the session.
     */
    fun unregisterAll(sessionId: String) {
        registryMap.remove(sessionId)
    }

    /**
     * Unregisters all listeners across all sessions.
     *
     */
    fun unregisterAllSessions() {
        registryMap.clear()
    }

    /**
     * Notifies all listeners across all sessions for a given affordance and optional form index.
     *
     * @param affordance The interaction affordance for which listeners are to be notified.
     * @param data The input data to pass to the listeners.
     * @param schema The schema to validate or transform the input data (optional).
     * @param formIndex The index of the form to notify listeners for (optional).
     *
     * Delegates to `ProtocolListenerRegistry.notify`.
     */
    suspend fun <T> notify(
        affordance: InteractionAffordance,
        data: InteractionInput,
        schema: DataSchema<T>? = null,
        formIndex: Int? = null
    ) {
        registryMap.values.forEach { registry ->
            registry.notify(affordance, data, schema, formIndex)
        }
    }
}