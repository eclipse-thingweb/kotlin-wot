package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
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

        val scraperAgent = ConversationalAgent.create(wot, "http://localhost:$port/scraper")
        val researchAgent = ConversationalAgent.create(wot, "http://localhost:$port/researcher")

        val latch = CountDownLatch(1)

        scraperAgent.consumeEvent("contentRetrieved") {
            val summary: String = researchAgent.chat("Summarize ${it.value().asText()} for me")
            logger.info(summary)
            latch.countDown()
        }

        scraperAgent.chat("Retrieve content from https://eclipse.dev/lmos/docs/lmos_protocol/introduction")

        latch.await()

    }
}