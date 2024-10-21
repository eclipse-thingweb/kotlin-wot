package ai.ancf.lmos.wot.test

import ai.ancf.lmos.wot.annotations.Action
import ai.ancf.lmos.wot.annotations.Property
import ai.ancf.lmos.wot.annotations.Thing


@Thing(title="Agent",
    description= "A simple agent.")
class Agent(val thingId: String) {

    @Property(title = "brightness", readOnly = true)
    var brightness: Int = 80

    @Action(title = "setBrightness")
    fun setBrightness(brightness: Brightness, level: Brightness): String {
        return "Brightness set to $brightness"
    }

    @Action(title = "turnOn")
    fun turnOn() : String {
        return "Lamp turned on"
    }
}

data class Brightness(val blub: Int, val bla: Int)

