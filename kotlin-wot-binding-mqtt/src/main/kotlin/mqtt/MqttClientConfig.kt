package ai.ancf.lmos.wot.binding.mqtt


data class MqttClientConfig(val broker: String,
                            val clientId: String,
                            private val username: String?,
                            private val password: String?) {
}

