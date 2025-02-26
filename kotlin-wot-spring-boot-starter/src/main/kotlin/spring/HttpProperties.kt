package ai.ancf.lmos.wot.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

open class ServerProperties(
    var enabled: Boolean = true,
    var host: String = "0.0.0.0",
    var port: Int = 8080,
    var baseUrls: List<String>
)

@ConfigurationProperties(prefix = "wot.servient.http.server", ignoreUnknownFields = true)
@Validated
class HttpServerProperties : ServerProperties(
    baseUrls = listOf("http://localhost:8080")
)

@ConfigurationProperties(prefix = "wot.servient.websocket.server", ignoreUnknownFields = true)
@Validated
class WebsocketProperties : ServerProperties(
    baseUrls = listOf("ws://localhost:8080")
)