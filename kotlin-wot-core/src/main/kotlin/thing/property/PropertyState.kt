package ai.ancf.lmos.wot.thing.property

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Supplier

class PropertyState<T> internal constructor(
    private val _flow: MutableSharedFlow<T>,
    initialValue: T?,
    var readHandler: Supplier<CompletableFuture<T>>? = null,
    var writeHandler: Function<T, CompletableFuture<T>>? = null
) {
    // Secondary constructor
    constructor() : this(MutableSharedFlow(), null)

    private val _value: MutableStateFlow<T?> = MutableStateFlow(initialValue)

    // Public flow to observe updates
    val flow: SharedFlow<T> = _flow

    // Getter for the current value
    val value: T?
        get() = _value.value

    // Emit value to the flow
    suspend fun emit(value: T) {
        _flow.emit(value)
    }

    // Update the value
    suspend fun setValue(newValue: T) {
        _value.value = newValue
        emit(newValue) // Emit the new value to the flow
    }

    // Function to read the current value asynchronously
    fun getValueAsync(): CompletableFuture<T?> {
        return CompletableFuture.completedFuture(value)
    }
}
