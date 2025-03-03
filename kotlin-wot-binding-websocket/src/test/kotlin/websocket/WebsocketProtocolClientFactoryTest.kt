/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.binding.mqtt

import org.eclipse.thingweb.binding.websocket.SecureWebSocketProtocolClientFactory
import org.eclipse.thingweb.binding.websocket.WebSocketProtocolClientFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class WebsocketProtocolClientFactoryTest {

    @Test
    fun getWsScheme() {
        assertEquals("ws", WebSocketProtocolClientFactory().scheme)
    }
    @Test
    fun getWssScheme() {
        assertEquals("wss", SecureWebSocketProtocolClientFactory().scheme)
    }
}

