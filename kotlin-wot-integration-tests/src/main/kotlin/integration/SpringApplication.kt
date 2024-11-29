package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


fun main(args: Array<String>) {
    runApplication<ThingAgentApplication>(*args)
}

@SpringBootApplication
class ThingAgentApplication : CommandLineRunner {

    @Autowired
    private lateinit var servient: Servient

    @Autowired
    private lateinit var wot: Wot

    @Autowired
    private lateinit var agent: ThingAgent

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ThingAgentApplication::class.java)
    }

    @PreDestroy
    fun onExit() {
        log.info("###STOPing###")
        log.info("###STOP FROM THE LIFECYCLE###")
    }

    override fun run(vararg args: String?) = runBlocking {

        // Protocol can be "HTTP" or "MQTT"
        val servient = createServient("HTTP")

        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Application is shutting down. Performing cleanup...")
            runBlocking { servient.shutdown() }
        })

        val wot = Wot.create(servient)
        val exposedThing = ExposedThingBuilder.createExposedThing(wot, agent, ThingAgent::class)

        // Start `servient` in a separate coroutine
        servient.start()
        // Add and expose the thing after `start()` has had time to begin
        servient.addThing(exposedThing as WoTExposedThing)
        servient.expose("agent")

        println("Exposed Agent on HTTP Server")
        Job().join()
    }
}

