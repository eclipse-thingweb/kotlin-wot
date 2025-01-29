package spring

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.routes.AbstractRoute.Companion.log
import ai.ancf.lmos.wot.reflection.ExposedThingBuilder
import ai.ancf.lmos.wot.reflection.annotations.Thing
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.web.server.WebServer
import org.springframework.boot.web.server.WebServerException
import org.springframework.context.ApplicationContext
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KClass

class WoTServer(val wot: Wot, val servient: Servient) : WebServer {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val latch = CountDownLatch(1)

    @Throws(WebServerException::class)
    override fun start() = runBlocking {
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
            // Ensure not server is left running
            servient.shutdown()
        }
    }

    @Throws(WebServerException::class)
    override fun stop() = runBlocking {
        servient.shutdown()
    }

    override fun getPort(): Int {
        return 0
    }
}