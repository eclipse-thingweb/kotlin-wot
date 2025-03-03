/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.mqtt

import kotlin.test.Test
import kotlin.test.assertEquals

class MqttProtocolClientFactoryTest {

    @Test
    fun getMqttScheme() {
        assertEquals("mqtt", MqttProtocolClientFactory(MqttClientConfig("test", 1, "test")).scheme)
    }
    @Test
    fun getMqttsScheme() {
        assertEquals("mqtts", MqttsProtocolClientFactory(MqttClientConfig("test", 2, "test")).scheme)
    }
}

