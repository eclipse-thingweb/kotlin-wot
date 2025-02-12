package ai.ancf.lmos.wot.binding.mqtt

import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client

open class MqttProtocolClientFactory(private val mqttClientConfig: MqttClientConfig) : ProtocolClientFactory {

    override fun toString(): String {
        return "MqttClient"
    }
    override val scheme: String
        get() = "mqtt"

    override suspend fun init() {

    }

    override suspend fun destroy() {

    }

    override fun createClient(): ProtocolClient =
        MqttProtocolClient(
            Mqtt5Client.builder()
                .identifier(mqttClientConfig.clientId)
                .serverHost(mqttClientConfig.host)
                .serverPort(mqttClientConfig.port)
                //.automaticReconnect()
                //.applyAutomaticReconnect()
                .build()
                .toAsync())

}
