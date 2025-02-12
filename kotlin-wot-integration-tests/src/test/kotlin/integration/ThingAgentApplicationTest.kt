package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.thing.ConsumedThing
import integration.ConversationalAgent
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
        val agent = ConversationalAgent.create(wot, "http://localhost:$port/chatagent")
        val answer = agent.chat("What is the state of my lamp?")
        logger.info(answer)
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