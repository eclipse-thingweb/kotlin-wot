package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.thing.schema.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PROPERTY_NAME = "propertyName"

private const val ACTION = "actionName"

class WoTIntegrationTest() {


    @Test
    fun `Should fetch thing`() = runTest {

        val servient = Servient(
            servers = listOf(HttpProtocolServer()),
            clientFactories = listOf(HttpProtocolClientFactory())
        )
        val wot = Wot.create(servient)

        val exposedThing = wot.produce {
            id = "myid"
            title = "MyThing"
            stringProperty(PROPERTY_NAME) {
                description = "Property description"
                minLength = 10
                readHandler = PropertyReadHandler { "propertyOutput" }
                writeHandler = PropertyWriteHandler { input -> input }
            }
            action<String, String>(ACTION){
                description = "Action description"
                input = stringSchema {
                    description = "Input description"
                }
                output = stringSchema {
                    description = "Output description"
                }
                actionHandler = ActionHandler { input, options -> "actionOutput: $input" }
            }
        }

        exposedThing.setPropertyReadHandler(PROPERTY_NAME) { "propertyOutput" }
        exposedThing.setPropertyWriteHandler<String>(PROPERTY_NAME) { input -> input }
        exposedThing.setActionHandler<String, String>(ACTION) { input, options -> "actionOutput: $input" }

        servient.start()
        servient.addThing(exposedThing)
        servient.expose("myid")

        //val fetchedThings = servient.fetchDirectory("http://localhost:8080")

        //assertEquals(1, fetchedThings.size)

        val thingDescription = wot.requestThingDescription("http://localhost:8080/myid")

        val consumedThing = wot.consume(thingDescription)

        assertEquals(consumedThing.id, exposedThing.id)

        val readProperty = consumedThing.readProperty(PROPERTY_NAME)

        assertEquals("propertyOutput", readProperty.value())

        val output = consumedThing.invokeAction(ACTION, "actionInput".toInteractionInputValue())

        assertEquals("actionOutput + actionInput", output.value())
    }
}