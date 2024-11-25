package reflection.things

import ai.ancf.lmos.wot.reflection.annotations.Action
import ai.ancf.lmos.wot.reflection.annotations.Event
import ai.ancf.lmos.wot.reflection.annotations.Property
import ai.ancf.lmos.wot.reflection.annotations.Thing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

@Thing(
    id = "complexThing",
    title = "Complex Thing",
    description = "A thing with complex properties, actions, and events."
)
class ComplexThing {

    // A nested configuration represented as a read-only property
    @Property(name = "nestedConfig", description = "A nested configuration object", readOnly = true)
    val nestedConfig: NestedConfig = NestedConfig(
        name = "defaultValue",
        values = listOf(1, 2, 3)
    )

    @Property(name = "exampleStringProperty", readOnly = true)
    val exampleStringProperty: String = "Hello World"

    @Property(name = "exampleIntProperty", readOnly = true)
    val exampleIntProperty: Int = 42

    @Property(name = "exampleBooleanProperty", readOnly = true)
    val exampleBooleanProperty: Boolean = true

    @Property(name = "exampleNumberProperty", readOnly = true)
    val exampleNumberProperty: Number = 3.14

    // New properties of specific types (Double, Float, and Long)
    @Property(name = "exampleDoubleProperty", readOnly = true)
    val exampleDoubleProperty: Double = 3.1415

    @Property(name = "exampleFloatProperty", readOnly = true)
    val exampleFloatProperty: Float = 2.71f

    @Property(name = "exampleLongProperty", readOnly = true)
    val exampleLongProperty: Long = 10000000000L

    @Property(name = "exampleIntArrayProperty", readOnly = true)
    val exampleIntArrayProperty: Array<Int> = arrayOf(1, 2, 3)

    @Property(name = "exampleStringArrayProperty", readOnly = true)
    val exampleStringArrayProperty: Array<String> = arrayOf("apple", "banana", "cherry")

    @Property(name = "exampleBooleanArrayProperty", readOnly = true)
    val exampleBooleanArrayProperty: Array<Boolean> = arrayOf(true, false, true)

    @Property(name = "exampleNumberArrayProperty", readOnly = true)
    val exampleNumberArrayProperty: Array<Number> = arrayOf(3.14, 42, 99.9)

    @Property(name = "exampleListProperty", readOnly = true)
    val exampleListProperty: List<String> = listOf("item1", "item2", "item3")

    @Property(name = "exampleSetProperty", readOnly = true)
    val exampleSetProperty: Set<String> = setOf("itemA", "itemB", "itemC")

    @Property(name = "exampleMapProperty", readOnly = true)
    val exampleMapProperty: Map<String, Int> = mapOf("key1" to 1, "key2" to 2, "key3" to 3)

    @Action(name = "processData")
    fun processData(input1: Int, input2: String): ComplexResult {
        return ComplexResult("Processed: $input2", input1 * 2)
    }

    @Action(name = "compute")
    fun compute(params: ComputationParams): ComputationResult {
        return ComputationResult(params.a + params.b, params.a * params.b)
    }

    @Action(name = "returnString")
    fun returnString(): String {
        return "Hello from action!"
    }

    @Action(name = "returnInt")
    fun returnInt(): Int {
        return 100
    }

    @Action(name = "returnBoolean")
    fun returnBoolean(): Boolean {
        return true
    }

    @Action(name = "doNothing")
    fun doNothing() {
        // This action returns nothing and does nothing
    }

    @Action(name = "processInput")
    fun processInput(input: String) {
        // This action takes an input and does not return anything
        println("Processed input: $input")
    }

    @Action(name = "sum")
    fun sum(a: Int, b: Int): Int {
        return a + b
    }

    // Event example for dynamic updates (optional for the test, but a useful addition)
    @Event(name = "statusChanged", description = "Fires when the status changes")
    fun statusChanged(): Flow<String> {
        return flow {
            emit("Status updated at ${System.currentTimeMillis()}") // Emit the result
        }
    }

    // Event stream that emits computation results periodically
    @Event(name = "computationUpdates", description = "Stream of periodic computation results")
    fun computationUpdates(): Flow<ComputationResult> {
        return flow {
            while (true) {
                // Simulate computation with random values
                val randomA = Random.nextInt(1, 100)
                val randomB = Random.nextInt(1, 100)
                val result = ComputationResult(
                    sum = randomA + randomB,
                    product = randomA * randomB
                )
                emit(result) // Emit the result
                delay(1000L) // Wait 1 second before emitting the next event
            }
        }
    }
}

// Data classes for input and output structures
data class NestedConfig(val name: String, val values: List<Int>)
data class ComplexResult(val message: String, val result: Int)
data class ComputationParams(val a: Int, val b: Int)
data class ComputationResult(val sum: Int, val product: Int)