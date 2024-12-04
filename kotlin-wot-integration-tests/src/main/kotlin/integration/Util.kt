package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolServer

private const val PROPERTY_NAME = "property1"
private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "ask"

private const val EVENT_NAME = "event1"

fun createServient(protocol: String): Servient {
    return when (protocol) {
        "HTTP" -> Servient(
            servers = listOf(HttpProtocolServer()),
            clientFactories = listOf(HttpProtocolClientFactory())
        )
        "MQTT" -> {
            val mqttConfig = MqttClientConfig("localhost", 61890, "wotServer")
            Servient(
                servers = listOf(MqttProtocolServer(mqttConfig))
            )
        }
        else -> throw IllegalArgumentException("Unsupported protocol: $protocol")
    }
}
