package ai.ancf.lmos.wot.binding.mqtt

import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client

open class MqttsProtocolClientFactory(private val mqttClientConfig: MqttClientConfig) : ProtocolClientFactory {
    override fun toString(): String {
        return "MqttClient"
    }
    override val scheme: String
        get() = "mqtts"
    override val client: MqttProtocolClient
        get() = MqttProtocolClient(Mqtt5Client.builder()
            .identifier(mqttClientConfig.clientId)
            .serverHost(mqttClientConfig.broker)
            .sslWithDefaultConfig()
            .automaticReconnect().applyAutomaticReconnect()
            .build().toAsync(), true)

    override suspend fun init() {
        client.start()
    }

    override suspend fun destroy() {
        client.stop()
    }
}
