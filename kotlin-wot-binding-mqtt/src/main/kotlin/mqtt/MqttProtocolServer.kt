package mqtt

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentCodecException
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.ActionAffordance
import ai.anfc.lmos.wot.binding.ProtocolServer
import ai.anfc.lmos.wot.binding.ProtocolServerException
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MqttProtocolServer(
    private val client : Mqtt5AsyncClient,
) : ProtocolServer {

    private val log: Logger = LoggerFactory.getLogger(MqttProtocolServer::class.java)
    private val things = mutableMapOf<String, ExposedThing>()
    private var started = false

    override suspend fun start(servient: Servient): Unit = coroutineScope {
        log.info("Connect Mqtt server client")
        started = true
        client.connect().await()
    }

    override suspend fun stop(): Unit = coroutineScope {
        log.info("Disconnect Mqtt server client")
        client.disconnect().await()
        started = false
    }

    override fun expose(thing: ExposedThing) {
        if (!started) throw ProtocolServerException("Server has not started yet")

        val baseUrl = createUrl()
        log.info("MqttServer exposes '{}' at '{}{}/*'", thing.id, baseUrl, thing.id)

        things[thing.id] = thing
        exposeProperties(thing, baseUrl)
        exposeActions(thing, baseUrl)
        exposeEvents(thing, baseUrl)
        exposeTD(thing)
        listenOnMqttMessages()
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

    private fun exposeProperties(thing: ExposedThing, baseUrl: String) {
        val properties = thing.properties

        properties.forEach { (name, property) ->
            val topic = "${thing.id}/properties/$name"

            val href = "$baseUrl$topic"
            val form = Form(href= href,
                contentType = ContentManager.DEFAULT,
                op = listOf(Operation.OBSERVE_PROPERTY, Operation.UNOBSERVE_PROPERTY))
            property.forms += (form)
            log.debug("Assigned '{}' to Property '{}'", href, name)
        }
    }

    private fun exposeActions(thing: ExposedThing, baseUrl: String) {
        val actions = thing.actions

        actions.forEach { (name, action) ->
            val topic = "${thing.id}/actions/$name"

            val href = "$baseUrl$topic"
            val form = Form(href= href,
                contentType = ContentManager.DEFAULT,
                op = listOf(Operation.INVOKE_ACTION))
            action.forms += (form)
            log.debug("Assigned '{}' to Action '{}'", href, name)
        }
    }

    private fun exposeEvents(thing: ExposedThing, baseUrl: String) {
        val events = thing.events

        events.forEach { (name, event) ->
            val topic = "${thing.id}/events/$name"
            val href = "$baseUrl$topic"
            val form = Form(href= href,
                contentType = ContentManager.DEFAULT,
                op = listOf(Operation.SUBSCRIBE_EVENT, Operation.UNSUBSCRIBE_EVENT),
                optionalProperties=  mapOf("mqtt:qos" to 0, "mqtt:retain" to false)
            )
            event.forms += (form)
            log.debug("Assigned '{}' to Event '{}'", href, name)
        }
    }

    private fun exposeTD(thing: ExposedThing) = runBlocking {
        val topic = thing.id
        log.debug("Publishing Thing Description '{}' to topic '{}'", thing.id, topic)

        try {
            val content = ContentManager.valueToContent(thing.toJson())
            val publishMessage = Mqtt5Publish.builder()
                .topic(topic)
                .payload(content.body)
                .retain(true)
                .build()
            client.publish(publishMessage).await()
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

    private fun listenOnMqttMessages() {
        client.toAsync().publishes(MqttGlobalPublishFilter.SUBSCRIBED) { message ->
            log.info("MqttServer received message for topic '{}'", message.topic.toString())
            val segments = message.topic.toString().split("/")
            if (segments.size >= 3) {
                val thingId = segments[0]
                val thing = things[thingId]
                if (thing != null && segments[1] == "actions") {
                    val actionName = segments[2]
                    val action = thing.actions[actionName]
                    actionMessageArrived(message, action)
                }
            } else {
                log.info("MqttServer received message for unexpected topic '{}'", message.topic.toString())
            }
        }
    }

    private fun actionMessageArrived(message: Mqtt5Publish, action: ActionAffordance<*, *>?) {
        action?.let {
            val inputContent = Content(ContentManager.DEFAULT, message.payloadAsBytes)
            try {
                val input = ContentManager.contentToValue(inputContent, action.input)
                //action.invoke(input)
            } catch (e: ContentCodecException) {
                log.warn("Unable to parse input", e)
            }
        } ?: log.warn("Action not found for topic: '{}'", message.topic.toString())
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
