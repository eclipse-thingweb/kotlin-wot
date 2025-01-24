package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
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
            wot.requestThingDescription("https://zion.vaimee.com/things/urn:uuid:7ba2bca0-a7f6-47b3-bdce-498caa33bbaf")

        val coffeeMachine = wot.consume(thingDescription)
        val resources = coffeeMachine.readProperty("resources").value() as DataSchemaValue.ObjectValue

        println(resources)
    }
}