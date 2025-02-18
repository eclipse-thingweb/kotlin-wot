package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
import integration.WotConversationalAgent
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
    fun testChat() = runBlocking {
        val agent = WotConversationalAgent.create(wot, "http://localhost:$port/chatagent")
        val answer = agent.chat("What is the state of my lamps?")
        logger.info(answer)
    }

    @Test
    fun testEventDriven() = runBlocking {
        // Construct the dynamic server URL

        val scraperAgent = WotConversationalAgent.create(wot, "http://localhost:$port/scraper")
        val researchAgent = WotConversationalAgent.create(wot, "http://localhost:$port/researcher")

        val latch = CountDownLatch(1)

        scraperAgent.consumeEvent("contentRetrieved") {
            val summary: String = researchAgent.chat("Summarize $it for me")
            logger.info(summary)
            latch.countDown()
        }

        scraperAgent.chat("Retrieve content from https://eclipse.dev/lmos/docs/lmos_protocol/introduction")

        latch.await()

    }
}