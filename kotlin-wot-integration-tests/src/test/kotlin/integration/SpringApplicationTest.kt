package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import kotlinx.coroutines.test.runTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ThingAgentApplicationTest {

    private var port: Int = 8080

    @Autowired
    private lateinit var wot: Wot

    @Test
    fun testGenericReadProperty() = runTest {
        // Construct the dynamic server URL
        val serverUrl = "http://localhost:$port/agent"

        // Request the Thing Description
        val thingDescription = wot.requestThingDescription(serverUrl)
        // Consume the Thing
        val testThing = wot.consume(thingDescription)

        // Read the Model Configuration property
        val modelConfiguration = testThing.genericReadProperty<ModelConfiguration>("modelConfiguration")
        assertNotNull(modelConfiguration, "Model Configuration should not be null")

        println("Model Configuration: $modelConfiguration")


    }
}