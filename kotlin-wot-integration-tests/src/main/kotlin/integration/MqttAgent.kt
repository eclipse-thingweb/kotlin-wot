package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
import kotlinx.coroutines.*


fun main(): Unit = runBlocking {
    val agent = Agent()

    // Protocol can be "HTTP" or "MQTT"
    val servient = createServient("MQTT")

    // Register a shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Application is shutting down. Performing cleanup...")
        launch { servient.shutdown() }
    })

    val wot = Wot.create(servient)
    val exposedThing = createExposedThing(wot, agent)

    // Start `servient` in a separate coroutine
    servient.start()
    // Add and expose the thing after `start()` has had time to begin
    servient.addThing(exposedThing)
    servient.expose("agent")
    println("Exposed Agent on MQTT Server")
    // Keep the application running until process is stopped
    Job().join()
}