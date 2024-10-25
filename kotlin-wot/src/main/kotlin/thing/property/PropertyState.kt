package ai.ancf.lmos.wot.thing.property

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import java.util.function.Supplier


class PropertyState<T>(
    private val _flow: MutableStateFlow<T>? = null, // Default initialization
    var readHandler: Supplier<CompletableFuture<T>>? = null,
    var writeHandler: Function<T, CompletableFuture<T>>? = null
) {

    // Getter for the current value
    val value: T? get() = _flow?.value

    // Emit value to the flow
    suspend fun emit(value: T) {
        _flow?.emit(value)
    }

    // Update the value
    suspend fun setValue(newValue: T) {
        _flow?.value = newValue
        emit(newValue) // Emit the new value to the flow
    }
}
