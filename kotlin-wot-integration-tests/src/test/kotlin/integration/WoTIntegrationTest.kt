package integration

import kotlinx.coroutines.test.runTest
import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import kotlin.test.Test

class WoTIntegrationTest {


    @Test
    fun `Should start http server`() = runTest {

        val servient = Servient(
            servers = listOf(HttpProtocolServer()),
            clientFactories = listOf(HttpProtocolClientFactory())
        )
        val wot = Wot.create(servient)

        val exposedThing = wot.produce {
            id = "myid"
            title = "MyThing"
        }

        servient.start()
        servient.addThing(exposedThing)
        servient.expose("myid")

        //val url = servient.fetchDirectory()

        //val consumedThing = servient.fetch(url)

    }
}