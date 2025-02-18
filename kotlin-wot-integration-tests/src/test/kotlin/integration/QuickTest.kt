package ai.ancf.lmos.wot.integration


import integration.WotConversationalAgent
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import kotlin.test.Test

class QuickTest {

    @Test
    fun `should control my lamp`() = runBlocking {
        val latch = CountDownLatch(3)

        val agent = WotConversationalAgent.create("http://localhost:8080/chatagent")
        agent.consumeEvent("agentEvent") {
            println("Event: $it")
            latch.countDown()
        }
        //val command = "What is the state of my lamp?"
        val command = "Turn all lamps on"
        println("User: $command")
        val answer = agent.chat(command)
        println("Agent: $answer")
        latch.await()
    }
}