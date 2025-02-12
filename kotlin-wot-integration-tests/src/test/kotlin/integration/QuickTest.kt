package integration

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class QuickTest {

    @Test
    fun `should control my lamp`() = runBlocking {
        val agent = ConversationalAgent.create("http://localhost:8080/chatagent")
        //val command = "What is the state of my lamp?"
        val command = "Turn on the lamp"
        println("User: $command")
        val answer = agent.chat(command)
        println("Agent: $answer")
    }
}