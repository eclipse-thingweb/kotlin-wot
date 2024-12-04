package ai.ancf.lmos.wot.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "wot.servient.http.server", ignoreUnknownFields = true)
@Validated
data class HttpServerProperties(
    var enabled: Boolean = true,
    var host: String = "localhost",
    var port: Int = 8080
)