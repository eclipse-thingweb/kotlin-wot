package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
        val thingDescription = wot.requestThingDescription("http://localhost:$port/agent")

        logger.warn(thingDescription.toString())

        val agent = wot.consume(thingDescription) as ConsumedThing
        val chat = Chat("Which resources are available?")

        val answer : String = agent.invokeAction(actionName = "ask", input = chat)

        logger.info(answer)

        // Read the Model Configuration property
        val modelConfiguration : ModelConfiguration = agent.genericReadProperty("modelConfiguration")

        logger.info("Model Configuration: $modelConfiguration")


    }
}