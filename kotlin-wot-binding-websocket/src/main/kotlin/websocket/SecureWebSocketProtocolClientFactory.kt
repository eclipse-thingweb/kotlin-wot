package websocket

/**
 * Creates new [HttpProtocolClient] instances that allow consuming Things via HTTPS.
 */
class SecureWebSocketProtocolClientFactory() : WebSocketProtocolClientFactory() {

    override fun toString(): String {
        return "SecureWebSocketProtocolClient"
    }

    override val scheme: String
        get() = "wss"
}
