package integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import ai.ancf.lmos.wot.thing.schema.stringSchema
import ai.ancf.lmos.wot.thing.schema.toInteractionInputValue
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
            }
            action<String, String>(ACTION){
                description = "Action description"
                input = stringSchema {
                    description = "Input description"
                }
                output = stringSchema {
                    description = "Output description"
                }
            }
        }

        //exposedThing.setPropertyWriteHandler(PROPERTY_NAME) { input -> input }
        //exposedThing.setActionHandler(ACTION) { input, options -> "actionOutput: $input" }

        servient.start()
        servient.addThing(exposedThing)
        servient.expose("myid")

        //val fetchedThings = servient.fetchDirectory("http://localhost:8080")

        //assertEquals(1, fetchedThings.size)

        val thingDescription = wot.requestThingDescription("http://localhost:8080/myid")

        val consumedThing = wot.consume(thingDescription)

        assertEquals(consumedThing.id, exposedThing.id)

        val readProperty = consumedThing.readProperty(PROPERTY_NAME)

        val propertyResponse = readProperty.value() as DataSchemaValue.StringValue

        assertEquals("propertyOutput", propertyResponse.value)

        val output = consumedThing.invokeAction(ACTION, "actionInput".toInteractionInputValue(), null)

        val actionResponse = output.value() as DataSchemaValue.StringValue

        assertEquals("actionOutput + actionInput", actionResponse.value)
    }
}