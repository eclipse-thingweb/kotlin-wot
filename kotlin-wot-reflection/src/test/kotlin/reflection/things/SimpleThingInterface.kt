package ai.ancf.lmos.wot.reflection.things

import ai.ancf.lmos.wot.reflection.annotations.Action

interface SimpleThingInterface {

    @Action(name = "outputAction")
    suspend fun outputAction() : String
}