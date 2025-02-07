package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ThingAgentApplicationTest {

    private val logger = LoggerFactory.getLogger("ThingAgentApplicationTest")

    private var port: Int = 8080

    @Autowired
    private lateinit var wot: Wot

    @Test
    fun testGenericReadProperty() = runBlocking {
        // Construct the dynamic server URL
        val thingDescription = wot.requestThingDescription("http://localhost:$port/chatagent")

        logger.warn(thingDescription.toString())

        val agent = wot.consume(thingDescription) as ConsumedThing
        val chat = Chat("Summarize https://eclipse.dev/lmos/docs/lmos_protocol/introduction for me")
        val answer : String = agent.invokeAction(actionName = "ask", input = chat)

        logger.info(answer)

        // Read the Model Configuration property
        val modelConfiguration : ModelConfiguration = agent.genericReadProperty("modelConfiguration")

        logger.info("Model Configuration: $modelConfiguration")
    }

    @Test
    fun testEventDriven() = runBlocking {
        // Construct the dynamic server URL
        val scraperAgent = wot.consume(wot.requestThingDescription("http://localhost:$port/scraper")) as ConsumedThing
        val researchAgent = wot.consume(wot.requestThingDescription("http://localhost:$port/researcher")) as ConsumedThing

        val latch = CountDownLatch(1)

        scraperAgent.subscribeEvent("contentRetrieved", listener =  {
            val summary : String = researchAgent.invokeAction("ask", Chat("Summarize ${it.value().asText()} for me"))
            logger.info(summary)
            latch.countDown()
        })

        val chat = Chat("Retrieve content from https://eclipse.dev/lmos/docs/lmos_protocol/introduction")
        scraperAgent.invokeUnitAction(actionName = "ask", input = chat)

        latch.await()

    }
}