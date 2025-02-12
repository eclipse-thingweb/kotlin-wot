package ai.ancf.lmos.wot.credentials


import ai.ancf.lmos.wot.security.*
import ai.ancf.lmos.wot.thing.schema.WoTForm
import org.slf4j.LoggerFactory

class DefaultCredentialsProvider(
    private val securitySchemes: List<SecurityScheme>,
    private val credentials: Map<String, Credentials>
) : CredentialsProvider {

    private val log = LoggerFactory.getLogger(DefaultCredentialsProvider::class.java)

    override fun getCredentials(form: WoTForm): Credentials? {
        if (securitySchemes.isEmpty()) {
            return null
        }
        return when (val security = securitySchemes.firstOrNull()) {
            is BasicSecurityScheme -> {
                val matchedCredentials = getMatchedCredentials(form)
                if (matchedCredentials is BasicCredentials) {
                    matchedCredentials
                } else {
                    throw NoCredentialsFound("Expected BasicCredentials but found ${matchedCredentials::class.simpleName}")
                }
            }

            is BearerSecurityScheme, is OAuth2SecurityScheme -> {
                val matchedCredentials = getMatchedCredentials(form)
                if (matchedCredentials is BearerCredentials) {
                    matchedCredentials
                } else {
                    throw NoCredentialsFound("Expected BearerCredentials but found ${matchedCredentials::class.simpleName}")
                }
            }

            is NoSecurityScheme -> null
            else -> {
                log.error("Cannot set security scheme '{}'", security)
                null
            }
        }
    }

    private fun getMatchedCredentials(form: WoTForm): Credentials {
        // Find the first credential where the key (URI) is contained in href
        val matchedCredentialKey = credentials.keys.firstOrNull { key -> form.href.contains(key) }
        val matchedCredentials = matchedCredentialKey?.let { credentials[it] }

        if (matchedCredentials == null) {
            throw NoCredentialsFound("No matching credentials found for href: ${form.href}")
        }
        return matchedCredentials

    }

}

class NoCredentialsFound(message: String) : RuntimeException(message)
