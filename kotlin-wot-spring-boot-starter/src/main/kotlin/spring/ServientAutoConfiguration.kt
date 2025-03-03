/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.spring

import org.eclipse.thingweb.Servient
import org.eclipse.thingweb.Wot
import org.eclipse.thingweb.binding.http.HttpProtocolClientFactory
import org.eclipse.thingweb.binding.http.HttpProtocolServer
import org.eclipse.thingweb.binding.http.HttpsProtocolClientFactory
import org.eclipse.thingweb.binding.mqtt.MqttClientConfig
import org.eclipse.thingweb.binding.mqtt.MqttProtocolClientFactory
import org.eclipse.thingweb.binding.mqtt.MqttProtocolServer
import org.eclipse.thingweb.binding.websocket.WebSocketProtocolClientFactory
import org.eclipse.thingweb.binding.websocket.WebSocketProtocolServer
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
@EnableConfigurationProperties(
    CredentialsProperties::class,
    HttpServerProperties::class,
    WebsocketProperties::class,
    MqttServerProperties::class,
    MqttClientProperties::class
)
class ServientAutoConfiguration {

    @Bean
    fun wotRuntime() = WoTRuntime()

    @Bean
    fun wot(servient : Servient) = Wot.create(servient)

    @Bean
    fun servient(
        servers : List<ProtocolServer>,
        clientFactories: List<ProtocolClientFactory>,
        credentialsProperties: CredentialsProperties
    ): Servient {
        return Servient(
            clientFactories = clientFactories,
            servers = servers,
            credentialStore = credentialsProperties.credentials.mapValues { it.value.convert() }
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
        fun webSocketProtocolServer(websocketProperties: WebsocketProperties): WebSocketProtocolServer {
            return WebSocketProtocolServer(
                bindHost = websocketProperties.host,
                bindPort = websocketProperties.port,
                baseUrls = websocketProperties.baseUrls
            )
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
            return HttpProtocolServer(
                bindHost = httpServerProperties.host,
                bindPort = httpServerProperties.port,
                baseUrls = httpServerProperties.baseUrls
            )
        }

        @Bean
        @ConditionalOnProperty(
            prefix = "wot.servient.http.client",
            name = ["enabled"],
            havingValue = "true",
            matchIfMissing = true // By default, enable the client
        )
        fun httpsProtocolClientFactory(): HttpsProtocolClientFactory {
            return HttpsProtocolClientFactory()
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