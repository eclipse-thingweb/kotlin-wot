package ai.ancf.lmos.wot.integration

import integration.ConversationalAgent
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class QuickTest {

    @Test
    fun `should control my lamp`() = runBlocking {
        val agent = ConversationalAgent.create("http://localhost:8080/chatagent")
        //val command = "What is the state of my lamp?"
        val command = "Set all my color lights to green and afterwards turn all of my lights on"
        println("User: $command")
        val answer = agent.chat(command)
        println("Agent: $answer")
    }
}