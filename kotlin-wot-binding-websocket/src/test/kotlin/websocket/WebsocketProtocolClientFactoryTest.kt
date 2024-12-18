package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.binding.websocket.SecureWebSocketProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
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

