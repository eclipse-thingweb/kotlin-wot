package ai.ancf.lmos.wot.binding.mqtt


data class MqttClientConfig(val host: String,
                            val port: Int,
                            val clientId: String,
                            private val username: String? = null,
                            private val password: String? = null) {
}

