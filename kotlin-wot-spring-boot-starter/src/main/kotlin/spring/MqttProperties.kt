/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "wot.servient.mqtt.server", ignoreUnknownFields = true)
@Validated
data class MqttServerProperties(
    var enabled: Boolean = true,
    var host: String = "localhost",
    var port: Int = 1883,
    var clientId : String = "wot-server",
    var username: String? = null,
    var password: String? = null
)

@ConfigurationProperties(prefix = "wot.servient.mqtt.client", ignoreUnknownFields = true)
@Validated
data class MqttClientProperties(
    var enabled: Boolean = true,
    var host: String = "localhost",
    var port: Int = 1883,
    var clientId : String = "wot-client",
    var username: String? = null,
    var password: String? = null
)