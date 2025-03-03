/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.spring

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder
import ai.ancf.lmos.wot.reflection.annotations.Thing
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KClass


class WoTRuntime : CommandLineRunner {

    @Autowired
    private lateinit var servient: Servient

    @Autowired
    private lateinit var wot: Wot

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val latch = CountDownLatch(1)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WoTRuntime::class.java)
    }

    @PreDestroy
    fun onExit() {
        // Register a shutdown hook
        log.debug("Application is shutting down. Performing cleanup...")
        runBlocking { servient.shutdown() }
        latch.countDown();
    }

    override fun run(vararg args: String?) = runBlocking {

        val things = applicationContext.getBeansWithAnnotation<Thing>()

        try{
            servient.start()
            for (thing in things.values) {
                // Cast clazz to KClass<T> explicitly
                @Suppress("UNCHECKED_CAST")
                val typedClass = thing::class as KClass<Any>
                val exposedThing = ExposedThingBuilder.createExposedThing(wot, thing, typedClass)
                // Add and expose the thing after `start()` has had time to begin
                servient.addThing(exposedThing as WoTExposedThing)
                servient.expose(exposedThing.getThingDescription().id)
            }

            log.info("Application is running... Press Ctrl+C to exit.");

            val awaitThread: Thread = object : Thread() {
                override fun run() {
                    latch.await()
                }
            }
            awaitThread.contextClassLoader = javaClass.classLoader
            awaitThread.isDaemon = false
            awaitThread.start()
        } catch (e : Exception){
            log.warn("Failed to start WoTRuntime", e)
            // Ensure not server is left running
            servient.shutdown()
        }
    }
}