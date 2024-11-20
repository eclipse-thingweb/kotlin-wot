package ai.ancf.lmos.wot.binding.http

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.security.BearerSecurityScheme
import ai.ancf.lmos.wot.security.NoSecurityScheme
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import http.HttpClientConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

/**
 * Allows consuming Things via HTTP.
 */
class HttpProtocolClient(
    private val httpClientConfig: HttpClientConfig? = null,
    private val client: HttpClient = HttpClient(CIO)
) : ProtocolClient {

    private var authorization: String? = null

    override suspend fun readResource(form: Form):Content {
        return resolveRequestToContent(form, HttpMethod.Get)
    }

    override suspend fun writeResource(form: Form, content: Content) {
        resolveRequestToContent(form, HttpMethod.Put, content)
    }

    override suspend fun invokeResource(form: Form, content: Content?): Content {
        return resolveRequestToContent(form, HttpMethod.Post, content)
    }

    override suspend fun subscribeResource(form: Form) = flow {
        // Long-polling logic using Ktor client
        while (true) {
            try {
                val response = client.get(form.href) {
                    timeout { requestTimeoutMillis = LONG_POLLING_TIMEOUT.toMillis() }
                }
                val content = checkResponse(response) // Convert the response to Content
                emit(content) // Emit the content downstream to flow collectors
            } catch (e: Exception) {
                throw e // Propagate the exception, Flow will handle it
            }
        }
    }.flowOn(Dispatchers.IO) // Run the flow on IO thread, as it involves network operations

    override suspend fun start() {
        TODO("Not yet implemented")
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

    override fun setSecurity(metadata: List<SecurityScheme>, credentials: Map<String, String>): Boolean {
        if (metadata.isEmpty()) {
            log.warn("HttpClient without security")
            return false
        }

        return when (val security = metadata.firstOrNull()) {
            is BasicSecurityScheme -> {
                val credentialsMap = credentials
                val username = credentialsMap["username"]
                val password = credentialsMap["password"]
                val basicAuth = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
                authorization = "Basic $basicAuth"
                true
            }
            is BearerSecurityScheme -> {
                val credentialsMap = credentials
                val token = credentialsMap["token"]
                authorization = "Bearer $token"
                true
            }
            is NoSecurityScheme -> true
            else -> {
                log.error("HttpClient cannot set security scheme '{}'", security)
                false
            }
        }
    }

    private suspend fun resolveRequestToContent(form: Form, method: HttpMethod, content: Content? = null): Content {
        return try {
            val response: HttpResponse = client.request(form.href) {
                this.method = method
                content?.let {
                    headers {
                        append(HttpHeaders.ContentType, it.type ?: ContentType.Application.Json.toString())
                    }
                    setBody(it.body)
                }
                authorization?.let { headers.append(HttpHeaders.Authorization, it) }
            }
            checkResponse(response)
        } catch (e: Exception) {
            throw ProtocolClientException("Error during http request: ${e.message}", e)
        }
    }

    private suspend fun checkResponse(response: HttpResponse): Content {
        return when (response.status.value) {
            in HttpStatusCode.OK.value..<HttpStatusCode.MultipleChoices.value -> {
                val body = response.readRawBytes()
                val contentType = response.contentType() ?: ContentType.Application.Json
                Content(contentType.toString(), body)
            }
            in HttpStatusCode.MultipleChoices.value..<HttpStatusCode.BadRequest.value -> {
                throw ProtocolClientException("Received ${response.status.value} and cannot continue (not implemented)")
            }
            else -> {
                throw ProtocolClientException("Server error: ${response.status}")
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(HttpProtocolClient::class.java)
        private const val HTTP_METHOD_NAME = "htv:methodName"
        private val LONG_POLLING_TIMEOUT = Duration.ofMinutes(60)
    }
}