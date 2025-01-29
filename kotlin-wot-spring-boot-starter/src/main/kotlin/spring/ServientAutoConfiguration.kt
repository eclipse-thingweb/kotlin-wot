package ai.ancf.lmos.wot.spring

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.binding.mqtt.MqttClientConfig
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolClientFactory
import ai.ancf.lmos.wot.binding.mqtt.MqttProtocolServer
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolClientFactory
import ai.ancf.lmos.wot.binding.websocket.WebSocketProtocolServer
import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import ai.anfc.lmos.wot.binding.ProtocolServer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@AutoConfiguration
@EnableConfigurationProperties(HttpServerProperties::class, MqttServerProperties::class, MqttClientProperties::class)
class ServientAutoConfiguration {

    @Bean
    fun wotRuntime() = WoTRuntime()

    @Bean
    fun wot(servient : Servient) = Wot.create(servient)

    @Bean
    fun servient(
        servers : List<ProtocolServer>,
        clientFactories: List<ProtocolClientFactory>
    ): Servient {
        return Servient(
            clientFactories = clientFactories,
            servers = servers
        )
    }

    @Configuration
    @ConditionalOnClass(WebSocketProtocolServer::class)
    class WebSocketConfiguration {

        @Bean
        @ConditionalOnProperty(
            prefix = "wot.servient.websocket.server",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the server
        )
        fun webSocketProtocolServer(httpServerProperties: HttpServerProperties): WebSocketProtocolServer {
            return WebSocketProtocolServer(bindHost = httpServerProperties.host, bindPort = httpServerProperties.port)
        }

        @Bean
        @ConditionalOnProperty(
            prefix = "wot.servient.websocket.client",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the client
        )
        fun websocketProtocolClientFactory(): WebSocketProtocolClientFactory {
            return WebSocketProtocolClientFactory()
        }
    }

    @Configuration
    @ConditionalOnClass(HttpProtocolServer::class)
    class HttpConfiguration {

        @Bean
        @ConditionalOnProperty(
            prefix = "wot.servient.http.server",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the server
        )
        fun httpProtocolServer(httpServerProperties: HttpServerProperties): HttpProtocolServer {
            return HttpProtocolServer(bindHost = httpServerProperties.host, bindPort = httpServerProperties.port)
        }

        @Bean
        @ConditionalOnProperty(
            prefix = "wot.servient.http.client",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the client
        )
        fun httpProtocolClientFactory(): HttpProtocolClientFactory {
            return HttpProtocolClientFactory()
        }
    }

    @Configuration
    @ConditionalOnClass(MqttProtocolServer::class)
    class MqttConfiguration {

        @Bean("mqttServerConfig")
        fun mqttServerConfig(mqttServerProperties: MqttServerProperties): MqttClientConfig {
            return MqttClientConfig(
                mqttServerProperties.host,
                mqttServerProperties.port,
                mqttServerProperties.clientId,
                mqttServerProperties.username,
                mqttServerProperties.password)
        }

        @Bean("mqttClientConfig")
        fun mqttClientConfig(mqttClientProperties: MqttClientProperties): MqttClientConfig {
            return MqttClientConfig(
                mqttClientProperties.host,
                mqttClientProperties.port,
                mqttClientProperties.clientId,
                mqttClientProperties.username,
                mqttClientProperties.password)
        }

        @Bean("mqttProtocolServer")
        @ConditionalOnProperty(
            prefix = "wot.servient.mqtt.server",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the server
        )
        fun mqttProtocolServer(@Qualifier("mqttServerConfig") mqttServerConfig : MqttClientConfig): MqttProtocolServer {
            return MqttProtocolServer(mqttServerConfig)
        }

        @ConditionalOnProperty(
            prefix = "wot.servient.mqtt.client",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the client
        )
        @Bean("mqttProtocolClientFactory")
        fun mqttProtocolClientFactory(@Qualifier("mqttClientConfig") mqttClientConfig : MqttClientConfig): MqttProtocolClientFactory {
            return MqttProtocolClientFactory(mqttClientConfig)
        }
    }
}