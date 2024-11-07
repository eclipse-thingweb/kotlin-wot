package ai.ancf.lmos.wot.binding.http.routes

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.thing.ExposedThingImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*

internal abstract class AbstractInteractionRoute(private val servient: Servient, private val securityScheme: String, private val  things: Map<String, ExposedThingImpl>
) : AbstractRoute() {

    private fun checkCredentials(securityScheme: String?, id: String, request: RoutingRequest): Boolean {
        if (securityScheme == null) {
            // No security configured -> always authorized
            return true
        }

        log.debug("HttpServer checking credentials for '{}'", id)
        val credentials = servient.getCredentials(id) as? Map<*, *> ?: return false

        val header = request.header(HttpHeaders.Authorization)?.takeIf { it.isNotBlank() }
        return when (securityScheme) {
            "Basic" -> {
                header?.removePrefix("Basic ")?.decodeBase64Bytes()?.toString(Charsets.UTF_8)?.let { decodedHeader ->
                    val (givenUsername, givenPassword) = decodedHeader.split(":", limit = 2)
                    val requiredUsername = credentials["username"] as? String
                    val requiredPassword = credentials["password"] as? String
                    givenUsername == requiredUsername && givenPassword == requiredPassword
                } ?: run {
                    log.warn("Unable to decode username and password from authorization header")
                    false
                }
            }

            "Bearer" -> {
                val requiredToken = credentials["token"] as? String
                header?.removePrefix("Bearer ") == requiredToken
            }

            else -> {
                log.warn("Unknown security scheme provided. Decline access.")
                false
            }
        }
    }
}
