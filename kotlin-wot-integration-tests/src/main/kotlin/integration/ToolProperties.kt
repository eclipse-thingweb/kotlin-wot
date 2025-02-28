package ai.ancf.lmos.wot.integration


import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "arc.ai")
data class ToolProperties(
    val tools: Map<String, Tool>
)

data class Tool(
    val url: String,
    val isDirectory: Boolean = false,
    val securityScheme: String = "nosec"
)