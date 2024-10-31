package ai.ancf.lmos.wot.thing.property

import kotlinx.coroutines.flow.MutableStateFlow


class PropertyState<T>(
    private val _flow: MutableStateFlow<T?> = MutableStateFlow(null),
    var readHandler: (suspend () -> T?)? = null,
    var writeHandler: (suspend (T) -> T?)? = null
) {

    // Getter for the current value
    val value: T? get() = _flow.value

    // Emit value to the flow
    private suspend fun emit(value: T) {
        _flow.emit(value)
    }

    // Update the value
    suspend fun setValue(newValue: T?) {
        _flow.value = newValue
        newValue?.let { emit(it) }
    }
}
