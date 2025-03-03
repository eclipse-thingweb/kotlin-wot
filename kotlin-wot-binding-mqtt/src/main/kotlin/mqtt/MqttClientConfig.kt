/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.mqtt

import java.util.*

data class MqttClientConfig(val host: String,
                            val port: Int,
                            val clientId: String = UUID.randomUUID().toString(),
                            private val username: String? = null,
                            private val password: String? = null)

