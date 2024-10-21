package ai.ancf.lmos.wot.thing.event

import kotlinx.coroutines.flow.MutableSharedFlow

class EventState<T> internal constructor(private val flow: MutableSharedFlow<T>) {

    constructor() : this(MutableSharedFlow())

    suspend fun emit(value: T) {
        flow.emit(value)
    }
}