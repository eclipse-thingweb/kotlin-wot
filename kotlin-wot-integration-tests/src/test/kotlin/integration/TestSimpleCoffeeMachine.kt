package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TestSimpleCoffeeMachine {

    @Test
    fun `Should fetch thing with HTTP`() = runTest {
        val http = HttpProtocolClientFactory()
        val https = HttpsProtocolClientFactory()
        val servient = Servient(clientFactories = listOf(http, https))

        val wot = Wot.create(servient)

        val thingDescription =
            wot.requestThingDescription("http://remotelab.esi.cit.tum.de:8080/virtual-coffee-machine-1_1")

        val testThing = wot.consume(thingDescription)
        val status = testThing.genericReadProperty<String>("status")

        val availableResources = testThing.genericReadProperty<Resources>("allAvailableResources")

        println(status)
        println(availableResources)
    }
}

data class Resources(
    val milk: Int,
    val water: Int ,
    val chocolate : Int,
    val coffeeBeans: Int
)