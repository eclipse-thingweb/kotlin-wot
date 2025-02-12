package ai.ancf.lmos.wot.spring


import ai.ancf.lmos.wot.credentials.ApiKeyCredentials
import ai.ancf.lmos.wot.credentials.BasicCredentials
import ai.ancf.lmos.wot.credentials.BearerCredentials
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "wot.servient.security")
@Validated
data class CredentialsProperties(
    var credentials: Map<String, Credentials> = emptyMap()
)

data class Credentials(
    val type: String, // to differentiate the credential type
    val token: String? = null, // for Bearer credentials
    val username: String? = null, // for Basic credentials
    val password: String? = null, // for Basic credentials
    val apiKey: String? = null // for API Key credentials
) {
    fun convert(): ai.ancf.lmos.wot.credentials.Credentials {
        return when (type) {
            "bearer" -> {
                if (token != null) {
                    BearerCredentials(token)
                } else {
                    throw IllegalArgumentException("Token is required for bearer credentials")
                }
            }
            "basic" -> {
                if (username != null && password != null) {
                    BasicCredentials(username, password)
                } else {
                    throw IllegalArgumentException("Username and password are required for basic credentials")
                }
            }
            "apikey" -> {
                if (apiKey != null) {
                    ApiKeyCredentials(apiKey)
                } else {
                    throw IllegalArgumentException("API Key is required for API Key credentials")
                }
            }
            else -> throw IllegalArgumentException("Unknown credentials type: $type")
        }
    }

}