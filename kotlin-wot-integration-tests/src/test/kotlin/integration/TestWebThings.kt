package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TestWebThings {

    @Test
    fun `Should control devices`() = runTest {
        val http = HttpProtocolClientFactory()
        val https = HttpsProtocolClientFactory()
        val servient = Servient(clientFactories = listOf(http, https))

        val wot = Wot.create(servient)

        val thingDescription = wot.requestThingDescription("https://plugfest.webthings.io/things/virtual-things-1")

        val testThing = wot.consume(thingDescription)
        val status = testThing.genericReadProperty<String>("status")

        val availableResources = testThing.genericReadProperty<Resources>("allAvailableResources")

        println(status)
        println(availableResources)
    }
}