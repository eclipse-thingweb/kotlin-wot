package ai.ancf.lmos.wot.binding.mqtt

import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client

open class MqttProtocolClientFactory(private val mqttClientConfig: MqttClientConfig) : ProtocolClientFactory {

    private val _client: MqttProtocolClient = MqttProtocolClient(
        Mqtt5Client.builder()
            .identifier(mqttClientConfig.clientId)
            .serverHost(mqttClientConfig.host)
            .serverPort(mqttClientConfig.port)
            //.automaticReconnect()
            //.applyAutomaticReconnect()
            .build()
            .toAsync()
    )

    override fun toString(): String {
        return "MqttClient"
    }
    override val scheme: String
        get() = "mqtt"
    override val client: MqttProtocolClient
        get() = _client

    override suspend fun init() {
        client.start()
    }

    override suspend fun destroy() {
        client.stop()
    }
}
