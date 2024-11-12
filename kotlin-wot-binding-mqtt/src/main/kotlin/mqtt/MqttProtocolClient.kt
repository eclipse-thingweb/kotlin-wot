package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.form.Form
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class MqttProtocolClient(
    private val client: Mqtt5AsyncClient,
    secure : Boolean = false
) : ProtocolClient {

    private val log = LoggerFactory.getLogger(MqttProtocolClient::class.java)
    private val scheme = "mqtt" + if (secure) "s" else ""

    override suspend fun invokeResource(form: Form, content: Content?): Content {
        val topic = try {
            URI(form.href).path.substring(1)
        } catch (e: URISyntaxException) {
            throw ProtocolClientException("Unable to extract topic from href '${form.href}'", e)
        }
        return requestReply(content, topic)
    }

    override suspend fun subscribeResource(form: Form): Flow<Content> {
        val topic = try {
            URI(form.href).path.substring(1)
        } catch (e: URISyntaxException) {
            throw ProtocolClientException("Unable to subscribe resource: ${e.message}")
        }

        return topicObserver(form, topic)
    }

    override suspend fun start() {
        client.connect().await()
    }

    override suspend fun stop() {
        client.disconnect().await()
    }

    // Function to observe a topic using HiveMQ Mqtt5AsyncClient
    private fun topicObserver(form: Form, topic: String): Flow<Content> = channelFlow {
        log.debug("MqttClient connected to broker at '{}:{}' subscribing to topic '{}'", client.config.serverHost, client.config.serverPort, topic)

        try {
            client.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)  // QoS level 1
                .send()
                .await()  // Suspending function for subscription completion

            client.publishes(MqttGlobalPublishFilter.SUBSCRIBED) { message ->
                log.debug("Received message from topic '{}'", topic)

                val content = Content(form.contentType, message.payloadAsBytes)  // Convert payload to Content
                trySend(content)
            }
        } catch (e: Exception) {
            log.warn("Error subscribing to topic '$topic': ${e.message}")
            close(e)  // Close flow on error
        }
    }.onCompletion {
        val client = client
        log.debug("No more observers for broker '{}' and topic '{}', unsubscribing.", client.config.serverHost, topic)

        try {
            client.unsubscribeWith()
                .topicFilter(topic)
                .send()
                .await()  // Await unsubscribe completion
        } catch (e: Exception) {
            log.warn("Error unsubscribing from topic '$topic': ${e.message}")
        }
    }

    // Function to publish content to a topic and return a response
    private suspend fun requestReply(content: Content?, topic: String): Content {
        // Generate a unique response topic for this request
        val responseTopic = "${topic}/reply/${UUID.randomUUID()}"

        try {
            log.debug(
                "Publishing to topic '{}' on broker '{}' with response expected on '{}'",
                topic,
                "${client.config.serverHost}:${client.config.serverPort}",
                responseTopic
            )

            val payload = content?.body

            // Prepare and send the publish message with a response topic
            val publishMessage = Mqtt5Publish.builder()
                .topic(topic)
                .payload(payload)
                .qos(MqttQos.AT_LEAST_ONCE)
                .responseTopic(responseTopic)  // Set the response topic
                .build()

            // Publish the message and await reply on the response topic
            return suspendCancellableCoroutine  { continuation ->

                client.subscribeWith()
                    .topicFilter(responseTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)  // QoS level 1 for reliability
                    .callback {
                        response ->
                        log.debug("Response message consumed from topic '$responseTopic'")
                        val replyContent = content?.type?.let { Content(it, response.payloadAsBytes) } ?: Content.EMPTY_CONTENT
                        continuation.resume(replyContent)

                        // Unsubscribe from the response topic after receiving the response
                        client.unsubscribeWith()
                            .topicFilter(responseTopic)
                            .send()
                    }
                    .send().thenAccept {
                        log.debug("Subscribed to topic '$responseTopic'")
                     }.exceptionally { e ->
                        log.warn("Failed to subscribe to topic '$responseTopic': ${e.message}", e)
                        continuation.resumeWithException(e)
                        null
                    }

                // Ensure the subscription is canceled if the coroutine is canceled
                continuation.invokeOnCancellation {
                    client.unsubscribe(Mqtt5Unsubscribe.builder().topicFilter(responseTopic).build())
                }

                // Publish the request message and await
                client.publish(publishMessage)
                    .thenAccept {
                        log.debug("Request message published to topic '$topic'")
                    }.exceptionally { e ->
                        log.warn("Failed to publish message to topic '$topic': ${e.message}", e)
                        continuation.resumeWithException(e)
                        null
                    }
            }
        } catch (e: Exception) {
            throw ProtocolClientException("Failed to execute request/reply on topic '$topic' with broker '${client.config.serverHost}': ${e.message}", e)
        }
    }

    override suspend fun readResource(form: Form): Content = suspendCancellableCoroutine { continuation ->
        val contentType = form.contentType ?: ContentManager.DEFAULT
        val requestUri = URL(form.href)

        // Extract the topic from the path, removing any leading "/"
        val filter = requestUri.path.removePrefix("/")

        try {

            Mqtt5Subscribe.builder().topicFilter(filter).build()

            // Subscribing to the topic
            val subscription = client.subscribe(Mqtt5Subscribe.builder().topicFilter(filter).build())
            { message ->
                val content = Content(contentType, message.payloadAsBytes)
                continuation.resume(content) // Resume the coroutine with the content

                // Unsubscribe after receiving the first message
                client.unsubscribeWith().topicFilter(filter)
            }

            // Ensure the subscription is canceled if the coroutine is canceled
            continuation.invokeOnCancellation {
                client.unsubscribeWith().topicFilter(filter)
            }

        } catch (e: Exception) {
            // Handle any exception during subscription
            continuation.resumeWithException(e)
        }
    }

    override suspend fun writeResource(form: Form, content: Content) {
        val requestUri = URL(form.href)
        val topic = requestUri.path.removePrefix("/")

        // Publishing message with optional retain and QoS settings
        val payload = content.body

        val publishMessage = Mqtt5Publish.builder()
            .topic(topic)
            .payload(payload)
            .qos(MqttQos.AT_LEAST_ONCE)
            .build()

        client.publish(publishMessage).await()
    }
}