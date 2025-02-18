package ai.ancf.lmos.wot.protocol

interface ConversationalAgent<I, O> {
    suspend fun chat(message: I): O
}

interface ConsumedConversationalAgent<I, O, E>: ConversationalAgent<I, O> {
    suspend fun consumeEvent(eventName: String, listener: EventListener<E>)
}

fun interface EventListener<E> {
    suspend fun handleEvent(data: E)
}


