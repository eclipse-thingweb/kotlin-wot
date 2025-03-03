/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.binding.mqtt

import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientFactory
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client

open class MqttsProtocolClientFactory(private val mqttClientConfig: MqttClientConfig) : ProtocolClientFactory {
    override fun toString(): String {
        return "MqttClient"
    }
    override val scheme: String
        get() = "mqtts"

    override suspend fun init() {
    }

    override suspend fun destroy() {
    }

    override fun createClient(): ProtocolClient =
        MqttProtocolClient(Mqtt5Client.builder()
            .identifier(mqttClientConfig.clientId)
            .serverHost(mqttClientConfig.host)
            .serverPort(mqttClientConfig.port)
            .sslWithDefaultConfig()
            .automaticReconnect().applyAutomaticReconnect()
            .build().toAsync(), true)
}
