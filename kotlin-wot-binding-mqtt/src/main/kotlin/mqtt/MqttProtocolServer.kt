package ai.ancf.lmos.wot.binding.mqtt

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.anfc.lmos.wot.binding.ProtocolServer
import ai.anfc.lmos.wot.binding.ProtocolServerException
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.datatypes.MqttTopic
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MqttProtocolServer(
    mqttClientConfig: MqttClientConfig
) : ProtocolServer {

    private val client : Mqtt5AsyncClient = Mqtt5Client.builder()
        .identifier(mqttClientConfig.clientId)
        .serverHost(mqttClientConfig.host)
        .serverPort(mqttClientConfig.port)
        //.automaticReconnect().applyAutomaticReconnect()
        .buildAsync()

    private val log: Logger = LoggerFactory.getLogger(MqttProtocolServer::class.java)
    private val things = mutableMapOf<String, ExposedThing>()
    private var started = false

    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        log.error("Caught exception: ${throwable.message}", throwable)
    }

    override suspend fun start(servient: Servient)  {
        log.info("Starting MqttProtocolServer")
        client.connect().await()
        started = true
        log.info("MqttProtocolServer started and connected to ${client.config.serverHost}:${client.config.serverPort} ")
    }

    override suspend fun stop() {
        log.info("Stopping MqttProtocolServer")
        client.disconnect().await()
        started = false
        log.info("MqttProtocolServer stopped")
    }

    override suspend fun expose(thing: ExposedThing) {
        if (!started) throw ProtocolServerException("Server has not started yet")

        val baseUrl = createUrl()
        log.debug("MqttServer exposes '{}' at '{}{}/*'", thing.id, baseUrl, thing.id)

        things[thing.id] = thing
        exposeProperties(thing, baseUrl)
        exposeActions(thing, baseUrl)
        exposeEvents(thing, baseUrl)
        exposeTD(thing)
        listenOnMqttMessages(thing)
    }

    override suspend fun destroy(thing: ExposedThing) {
        log.info("MqttServer stop exposing '{}' as unique '/{}/*'", thing.id, thing.id)

        unexposeTD(thing)
        things.remove(thing.id)
    }

    private fun createUrl(): String {
        val brokerUrl = "mqtt://${client.config.serverHost}:${client.config.serverPort}"
        return if (brokerUrl.endsWith("/")) brokerUrl else "$brokerUrl/"
    }

    private suspend fun exposeProperties(thing: ExposedThing, baseUrl: String) {
        val properties = thing.properties

        properties.forEach { (name, property) ->
            val topic = "${thing.id}/properties/$name"

            val href = "$baseUrl$topic"

            // Determine the operations based on readOnly/writeOnly status
            val operations = when {
                property.readOnly -> listOf(Operation.READ_PROPERTY)
                property.writeOnly -> listOf(Operation.WRITE_PROPERTY)
                else -> listOf(Operation.READ_PROPERTY, Operation.WRITE_PROPERTY)
            }

            // Create the main form and add it to the property
            val form = Form(
                href = href,
                contentType = "application/json",
                op = operations
            )
            property.forms += form
            log.debug("Assign '{}' to Property '{}'", href, name)

            // If the property is observable, add an additional form with an observable href
            if (property.observable) {
                val observableTopic = "${thing.id}/properties/$name/observable"
                val observableHref = "$baseUrl$observableTopic"

                val observableForm = Form(
                    href = observableHref,
                    contentType = "application/json",
                    op = listOf(Operation.OBSERVE_PROPERTY, Operation.UNOBSERVE_PROPERTY)
                )
                property.forms += observableForm
                log.debug("Assign '{}' to observe Property '{}'", observableHref, name)

                val observeListener = ContentListener { content ->
                    log.debug("MqttServer at $baseUrl publishing to Property topic '$observableTopic'")
                    val buffer = content.body
                    val publishMessage = Mqtt5Publish.builder()
                        .topic(observableTopic)
                        .payload(buffer)
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .retain(false)
                        .build()
                    client.publish(publishMessage).await()
                }
                thing.handleObserveProperty(propertyName = name, listener = observeListener)
            }
        }
    }

    private fun exposeActions(thing: ExposedThing, baseUrl: String) {
        val actions = thing.actions

        actions.forEach { (name, action) ->
            val topic = "${thing.id}/actions/$name"

            val href = "$baseUrl$topic"
            val form = Form(href= href,
                contentType = ContentManager.DEFAULT_MEDIA_TYPE,
                op = listOf(Operation.INVOKE_ACTION))
            action.forms += (form)
            log.debug("Assigned '{}' to Action '{}'", href, name)
        }
    }

    private suspend fun exposeEvents(thing: ExposedThing, baseUrl: String) {
        val events = thing.events

        events.forEach { (name, event) ->
            val topic = "${thing.id}/events/$name"
            val href = "$baseUrl$topic"
            val form = Form(href= href,
                contentType = ContentManager.DEFAULT_MEDIA_TYPE,
                op = listOf(Operation.SUBSCRIBE_EVENT, Operation.UNSUBSCRIBE_EVENT),
                optionalProperties=  mapOf("mqtt:qos" to 0, "mqtt:retain" to false)
            )
            event.forms += (form)
            log.debug("Assigned '{}' to Event '{}'", href, name)

            thing.handleSubscribeEvent(eventName = name, listener = { content ->
                log.debug("MqttServer at $baseUrl publishing to Events topic '$topic")
                val publishMessage = Mqtt5Publish.builder()
                    .topic(topic)
                    .payload(content.body)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .retain(false)
                    .build()
                client.publish(publishMessage).await()
            })
        }
    }

    private suspend fun exposeTD(thing: ExposedThing) {
        val topic = thing.id
        //log.debug("Publishing Thing Description '{}' to topic '{}'", thing.id, topic)

        try {
            val content = ContentManager.valueToContent(thing.toJson(), "application/json")
            val tdTopic = thing.id
            client.subscribeWith()
                .topicFilter(tdTopic)
                .qos(MqttQos.AT_LEAST_ONCE) // Use AT_LEAST_ONCE QoS level
                .callback { message ->
                    val responseTopic = message.responseTopic.get()
                    log.debug("Sending Thing Description of thing '{}' to topic '{}'", thing.id, responseTopic)
                    CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                        respondToTopic(content, responseTopic)
                    }
                }
                .send().await()  // Sending the subscription request
            /*
            val publishMessage = Mqtt5Publish.builder()
                .topic(topic)
                .payload(content.body)
                .retain(true)
                .build()
            client.publish(publishMessage).await()
            */
        } catch (e: Exception) {
            log.warn("Unable to publish thing description to topic '{}': {}", topic, e.message)
        }
    }

    private fun unexposeTD(thing: ExposedThing) = runBlocking {
        val topic = thing.id
        log.debug("Removing published Thing Description for topic '{}'", topic)

        try {
            val publishMessage = Mqtt5Publish.builder()
                .topic(topic)
                .retain(true)
                .build()
            client.publish(publishMessage).await()
        } catch (e: Exception) {
            log.warn("Unable to remove thing description from topic '{}': {}", topic, e.message)
        }
    }

    private suspend fun listenOnMqttMessages(thing: ExposedThing) {
        val sharedSubscriptionTopic = "\$share/server-group/${thing.id}/+/+"

        val sharedSubscriptionTopicProperties = "\$share/server-group/${thing.id}/properties/+"
        val sharedSubscriptionTopicActions = "\$share/server-group/${thing.id}/actions/+"

        client.subscribeWith()
            .topicFilter(sharedSubscriptionTopicProperties)
            .qos(MqttQos.AT_LEAST_ONCE) // Use AT_LEAST_ONCE QoS level
            .callback { message ->
                val messageTopic = message.topic.toString()
                log.info("MqttServer received message for topic '{}'", messageTopic)

                val segments = messageTopic.split("/")
                if (segments.size < 3) {
                    log.info("Unexpected topic format '{}'", sharedSubscriptionTopic)
                    return@callback
                }

                val thingId = segments[0]
                val exposedThing = things[thingId]
                if (exposedThing == null) {
                    log.warn("Thing with id '{}' not found", thingId)
                    return@callback
                }

                CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                    handlePropertyMessage(thing, segments, message)
                }

            }
            .send().await()  // Sending the subscription request

        client.subscribeWith()
            .topicFilter(sharedSubscriptionTopicActions)
            .qos(MqttQos.AT_LEAST_ONCE) // Use AT_LEAST_ONCE QoS level
            .callback { message ->
                val messageTopic = message.topic.toString()
                log.info("MqttServer received message for topic '{}'", messageTopic)

                val segments = messageTopic.split("/")
                if (segments.size < 3) {
                    log.info("Unexpected topic format '{}'", sharedSubscriptionTopic)
                    return@callback
                }

                val thingId = segments[0]
                val exposedThing = things[thingId]
                if (exposedThing == null) {
                    log.warn("Thing with id '{}' not found", thingId)
                    return@callback
                }

                CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                    handleActionMessage(thing, segments, message)
                }

            }
            .send().await()  // Sending the subscription request
    }

    private suspend fun handlePropertyMessage(thing: ExposedThing, segments: List<String>, message: Mqtt5Publish) {
        val propertyName = segments.getOrNull(2) ?: return log.warn("Property name missing in topic")
        val property = thing.properties[propertyName]
        if (property == null) {
            log.warn("Property '{}' not found on thing '{}'", propertyName, thing.id)
            return
        }

        if (message.payload.isEmpty) {
            // If no payload, consider it a read request
            val responseContent = thing.handleReadProperty(propertyName)
            respondToTopic(responseContent, message.responseTopic.get())
        } else {
            // If payload is provided, consider it a write request
            val inputContent = Content(ContentManager.DEFAULT_MEDIA_TYPE, message.payloadAsBytes)
            val responseContent = thing.handleWriteProperty(propertyName, inputContent)
            respondToTopic(responseContent, message.responseTopic.get())
        }
    }

    private suspend fun handleActionMessage(thing: ExposedThing, segments: List<String>, message: Mqtt5Publish) {
        val actionName = segments.getOrNull(2) ?: return log.warn("Action name missing in topic")
        val action = thing.actions[actionName]
        if (action == null) {
            log.warn("Action '{}' not found on thing '{}'", actionName, thing.id)
            return
        }
        val inputContent = Content(ContentManager.DEFAULT_MEDIA_TYPE, message.payloadAsBytes)
        val actionResult = thing.handleInvokeAction(actionName, inputContent)
        respondToTopic(actionResult, message.responseTopic.get())
    }

    private suspend fun handleEventSubscriptionMessage(thing: ExposedThing, segments: List<String>, message: Mqtt5Publish) {
        val eventName = segments.getOrNull(2) ?: return log.warn("Event name missing in topic")
        val event = thing.events[eventName]
        if (event == null) {
            log.warn("Event '{}' not found on thing '{}'", eventName, thing.id)
            return
        }

        val contentListener = ContentListener { content ->
            respondToTopic(content, message.topic)
        }
        thing.handleSubscribeEvent(eventName = eventName, listener = contentListener)
    }

    private suspend fun respondToTopic(content: Content?, responseTopic: MqttTopic) {
        try {
            val payload = content?.body
            val publishMessage = Mqtt5Publish.builder()
                .topic(responseTopic)
                .payload(payload)
                .qos(MqttQos.AT_LEAST_ONCE)
                .build()

            client.publish(publishMessage).await()
            log.info("Response sent to topic '{}'", responseTopic)
        } catch (e: Exception) {
            log.warn("Failed to send response to topic '{}': {}", responseTopic, e.message)
        }
    }
}

data class MqttBrokerServerConfig(
    val uri: String,
    val user: String? = null,
    val psw: String? = null,
    val clientId: String? = null,
    val protocolVersion: Int? = 5,  // Defaulting to 5 if none specified
    val rejectUnauthorized: Boolean? = null,
    val selfHost: Boolean? = null,
    val selfHostAuthentication: List<MqttClientConfig>? = null
)
