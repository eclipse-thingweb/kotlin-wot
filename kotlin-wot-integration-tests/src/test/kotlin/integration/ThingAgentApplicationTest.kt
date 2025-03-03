/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.sdk.agents.WotConversationalAgent
import ai.ancf.lmos.sdk.agents.lastMessage
import ai.ancf.lmos.sdk.agents.toAgentRequest
import ai.ancf.lmos.wot.Wot
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
        val answer = agent.chat("What is the state of my lamps?".toAgentRequest())
        logger.info(answer.lastMessage())
    }

    @Test
    fun testEventDriven() = runBlocking {
        // Construct the dynamic server URL

        val scraperAgent = WotConversationalAgent.create(wot, "http://localhost:$port/scraper")
        val researchAgent = WotConversationalAgent.create(wot, "http://localhost:$port/researcher")

        val latch = CountDownLatch(1)

        scraperAgent.consumeEvent("contentRetrieved", String::class) {
            val summary: String = researchAgent.chat("Summarize $it for me".toAgentRequest()).lastMessage()
            logger.info(summary)
            latch.countDown()
        }

        scraperAgent.chat("Retrieve content from https://eclipse.dev/lmos/docs/lmos_protocol/introduction".toAgentRequest())

        latch.await()

    }
}