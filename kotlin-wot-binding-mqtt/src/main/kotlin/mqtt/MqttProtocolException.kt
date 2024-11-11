package ai.ancf.lmos.wot.binding.mqtt


internal class MqttProtocolException : Exception {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}

