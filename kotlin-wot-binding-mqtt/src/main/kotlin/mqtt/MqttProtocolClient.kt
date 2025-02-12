package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.credentials.CredentialsProvider
import ai.ancf.lmos.wot.thing.schema.WoTForm
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class MqttProtocolClient(
    private val client: Mqtt5AsyncClient,
    secure : Boolean = false
) : ProtocolClient {

    private val log = LoggerFactory.getLogger(MqttProtocolClient::class.java)
    private val scheme = "mqtt" + if (secure) "s" else ""

    private val topicChannels = ConcurrentHashMap<String, Channel<Content>>()

    override suspend fun invokeResource(form: WoTForm, content: Content?): Content {
        val topic = try {
            URI(form.href).path.substring(1)
        } catch (e: URISyntaxException) {
            throw ProtocolClientException("Unable to extract topic from href '${form.href}'", e)
        }
        return requestReply(form, content, topic)
    }

    override suspend fun subscribeResource(form: WoTForm): Flow<Content> {
        val topic = try {
            URI(form.href).path.substring(1)
        }
        catch (e: URISyntaxException) {
            throw ProtocolClientException("Unable to subscribe resource: ${e.message}")
        }
        return subscribeToTopic(form, topic)
    }

    override suspend fun unlinkResource(form: WoTForm) {
        val topic = try {
            URI(form.href).path.substring(1)
        }
        catch (e: URISyntaxException) {
            throw ProtocolClientException("Unable to unlink resource: ${e.message}")
        }
        return unsubscribeFromTopic(topic)
    }

    override suspend fun start() {
        client.connect().await()
    }

    override suspend fun stop() {
        client.disconnect().await()
    }

    // Function to unsubscribe from a topic and close the associated channel
    private suspend fun unsubscribeFromTopic(topic: String) {
        // Check if the topic has an associated channel in the ConcurrentHashMap
        val channel = topicChannels.remove(topic)

        if (channel != null) {
            try {
                // Unsubscribe from the topic
                client.unsubscribeWith()
                    .topicFilter(topic)
                    .send()
                    .await()
                log.debug("Unsubscribed from topic '{}'", topic)
            } catch (e: Exception) {
                log.warn("Error unsubscribing from topic '$topic': ${e.message}")
            }

            // Close the channel
            channel.close()
            log.debug("Closed channel for topic '{}'", topic)
        } else {
            log.warn("No active channel found for topic '{}'", topic)
        }
    }

    // Function to observe a topic using HiveMQ Mqtt5AsyncClient
    private suspend fun subscribeToTopic(form: WoTForm, topic: String): Flow<Content> {
        log.debug("MqttClient connected to broker at '{}:{}' subscribing to topic '{}'", client.config.serverHost, client.config.serverPort, topic)

        // Create a channel for the topic
        val channel = Channel<Content>()
        // Put the channel in the ConcurrentHashMap
        topicChannels[topic] = channel

        try {
            client.subscribeWith()
                .topicFilter(topic)
                .callback() { message ->
                    log.debug("Received message from topic '{}'", topic)
                    val content = Content(form.contentType, message.payloadAsBytes)  // Convert payload to Content
                    val channelResult = channel.trySend(content)
                    log.debug("Send message to channel flow")
                }
                .send()
                .await()

            log.debug("Subscribed to topic '{}'", topic)
        } catch (e: Exception) {
            log.warn("Error subscribing to topic '$topic': ${e.message}")
            channel.close(e)
            //close(e)  // Close flow on error
        }
        return channel.consumeAsFlow()
    }

        /*
        .onCompletion {
        val client = client
        log.debug("No flow collectors anymore, unsubscribing.", client.config.serverHost, topic)
        try {
            client.unsubscribeWith()
                .topicFilter(topic)
                .send()
                .await()  // Await unsubscribe completion
        } catch (e: Exception) {
            log.warn("Error unsubscribing from topic '$topic': ${e.message}")
        }
    }
    */


    // Function to publish content to a topic and return a response
    private suspend fun requestReply(form: WoTForm, content: Content?, topic: String): Content {
        // Generate a unique response topic for this request
        val responseTopic = "${topic}/reply/${UUID.randomUUID()}"

        try {
            log.debug(
                "Publishing message to topic '{}' on broker '{}' with response expected on '{}'",
                topic,
                "${client.config.serverHost}:${client.config.serverPort}",
                responseTopic
            )

            val payload = content?.body
            val contentType = form.contentType

            // Prepare and send the publish message with a response topic
            val publishMessage = Mqtt5Publish.builder()
                .topic(topic)
                .payload(payload)
                .contentType(contentType)
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
                        val replyContent = Content(contentType, response.payloadAsBytes)
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

    override fun setCredentialsProvider(credentialsProvider: CredentialsProvider) {
    }

    // Function to read the resource using the request-reply pattern
    override suspend fun readResource(form: WoTForm): Content {
        // Extract the content type from the form or use a default if not provided

        // Extract the topic from the URI
        val requestUri = URI(form.href)
        val topic = requestUri.path.removePrefix("/") // Removing leading "/"

        try {
            // Call requestReply to send a request and get a reply
            return requestReply(form, null, topic)  // Passing 'null' for content if it's just a read request
        } catch (e: Exception) {
            // Handle any exception during request-reply
            throw ProtocolClientException("Failed to read resource from topic '$topic'", e)
        }
    }

    // Function to write the resource using the request-reply pattern
    override suspend fun writeResource(form: WoTForm, content: Content) {
        // Extract the topic from the URI
        val requestUri = URI(form.href)
        val topic = requestUri.path.removePrefix("/") // Removing leading "/"

        try {
            // Call requestReply to send the content and get the reply
            requestReply(form, content, topic)  // Send content to the topic and expect a reply
        } catch (e: Exception) {
            // Handle any exception during request-reply
            throw ProtocolClientException("Failed to write resource to topic '$topic'", e)
        }
    }
}