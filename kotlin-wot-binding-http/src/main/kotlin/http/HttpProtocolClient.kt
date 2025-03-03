/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.binding.http

import org.eclipse.thingweb.content.Content
import org.eclipse.thingweb.credentials.CredentialsProvider
import org.eclipse.thingweb.thing.schema.WoTForm
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import io.ktor.client.*
import io.ktor.client.engine.*
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

/**
 * Allows consuming Things via HTTP.
 */
class HttpProtocolClient(
    private val client: HttpClient = createHttpClient()
) : ProtocolClient {

    private var credentialsProvider: CredentialsProvider? = null

    override suspend fun readResource(form: WoTForm):Content {
        return resolveRequestToContent(form, HttpMethod.Get)
    }

    override suspend fun writeResource(form: WoTForm, content: Content) {
        resolveRequestToContent(form, HttpMethod.Put, content)
    }

    override suspend fun invokeResource(form: WoTForm, content: Content?): Content {
        return resolveRequestToContent(form, HttpMethod.Post, content)
    }

    override suspend fun subscribeResource(form: WoTForm) = flow {
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

    override fun setCredentialsProvider(credentialsProvider: CredentialsProvider){
        this.credentialsProvider = credentialsProvider
    }

    private suspend fun resolveRequestToContent(form: WoTForm, method: HttpMethod, content: Content? = null): Content {
        return try {
            val response: HttpResponse = client.request(form.href) {
                headers {
                    append(HttpHeaders.Accept, form.contentType)
                }
                this.method = method
                content?.let {
                    headers {
                        append(HttpHeaders.ContentType, form.contentType )
                    }
                    setBody(it.body)
                }
                credentialsProvider?.getCredentials(form).let { headers.append(HttpHeaders.Authorization, it.toString()) }
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

fun createHttpClient(): HttpClient {
    val proxyHost = System.getProperty("http.proxyHost")
    val proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull()

    return if (!proxyHost.isNullOrBlank() && proxyPort != null) {
        HttpClient(CIO) {
            val proxyUrl = URLBuilder().apply {
                protocol = URLProtocol.HTTP
                host = proxyHost
                port = proxyPort
            }.build()
            engine {
                proxy = ProxyBuilder.http(proxyUrl)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 50000
            }
        }
    } else {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 50000
            }
        }
    }
}