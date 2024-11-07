package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val servient = Servient(
        servers = listOf(HttpProtocolServer(wait = true)),
        clientFactories = listOf(HttpProtocolClientFactory())
    )

    // Register a shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Application is shutting down. Performing cleanup...")
        launch { servient.shutdown() }
    })

    val wot = Wot.create(servient)

    val exposedThing = wot.produce {
        id = "myid"
        title = "MyThing"
        stringProperty("test"){
            title = "myProperty"
            minLength = 10
        }
    }

    // Start `servient` in a separate coroutine
    val startJob = launch(Dispatchers.IO) {
        servient.start()
    }

    // Add and expose the thing after `start()` has had time to begin
    servient.addThing(exposedThing)
    servient.expose("myid")

    // Keep the coroutine active as long as `servient.start()` is running
    startJob.join()
}