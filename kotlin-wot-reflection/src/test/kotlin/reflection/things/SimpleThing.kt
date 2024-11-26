package reflection.things

import ai.ancf.lmos.wot.reflection.annotations.Action
import ai.ancf.lmos.wot.reflection.annotations.Event
import ai.ancf.lmos.wot.reflection.annotations.Property
import ai.ancf.lmos.wot.reflection.annotations.Thing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

@Thing(
    id = "simpleThing",
    title = "Simple Thing",
    description = "A thing with complex properties, actions, and events."
)
class SimpleThing {

    var counter = 0

    @Property(name = "observableProperty", title = "Observable Property", readOnly = true)
    val observableProperty : MutableStateFlow<String> = MutableStateFlow("Hello World")

    @Property(name = "mutableProperty")
    var mutableProperty: String = "test"

    @Property(name = "readyOnlyProperty", readOnly = true)
    val readyOnlyProperty: String = "test"

    @Property(name = "writeOnlyProperty", writeOnly = true)
    var writeOnlyProperty: String = "test"

    @Action(name = "voidAction")
    fun voidAction() {
        println("Action executed")
        counter += 1
    }

    @Action(name = "changeObservableProperty")
    fun changeObservableProperty(){
        observableProperty.value = "Hello from action!"
    }

    @Action(name = "outputAction")
    fun outputAction() : String {
        return "test"
    }

    @Action(name = "inputAction")
    fun inputAction(input : String) {
        println("Action executed")
        counter += 1
    }

    @Action(name = "inOutAction")
    fun inOutAction(input : String) : String {
        println("Action executed")
        return "$input output"
    }

    @Event(name = "statusUpdated")
    fun statusUpdated(): Flow<String> {
        return flow {
            emit("Status updated")
        }
    }
}