package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpProtocolServer
import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.schema.*
import integration.Agent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val PROPERTY_NAME = "property1"
private const val PROPERTY_NAME_2 = "property2"

private const val ACTION_NAME = "ask"

private const val ACTION_NAME_2 = "action2"

private const val ACTION_NAME_3 = "action3"

private const val ACTION_NAME_4 = "action4"

private const val EVENT_NAME = "event1"

fun main(): Unit = runBlocking {
    val servient = Servient(
        servers = listOf(HttpProtocolServer(wait = true)),
        clientFactories = listOf(HttpProtocolClientFactory())
    )

    // Register a shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Application is shutting down. Performing cleanup...")
        launch { servient.shutdown() }
    })


    val wot = Wot.create(servient)

    val exposedThing = wot.produce {
        id = "Agent"
        objectType = Type("Agent")
        intProperty(PROPERTY_NAME) {
            observable = true
        }
        intProperty(PROPERTY_NAME_2) {
            observable = true
        }
        action<String, Map<*,*>>(ACTION_NAME)
        {
            title = ACTION_NAME
            description = "Ask a question to the agent"
            input = stringSchema {
                title = "Action Input"
                description = "Question"
                minLength = 10
                default = "test"
            }
            output = objectSchema {
                stringProperty("response") {
                    description = "Answer of the agent"
                }
            }
        }
        action<String, String>(ACTION_NAME_2)
        {
            title = ACTION_NAME_2
            output = StringSchema()
        }
        action<String, String>(ACTION_NAME_3)
        {
            title = ACTION_NAME_3
            input = StringSchema()
        }
        action<String, String>(ACTION_NAME_4)
        {
            title = ACTION_NAME_4
        }
        event<String, Nothing, Nothing>(EVENT_NAME) {
            data = StringSchema()
        }
    }

    val agent = Agent()

    exposedThing.setPropertyReadHandler(PROPERTY_NAME) {
        10.toInteractionInputValue()
    }.setPropertyReadHandler(PROPERTY_NAME_2) {
        5.toInteractionInputValue()
    }.setActionHandler(ACTION_NAME) { input, _->
        val inputString = input.value() as DataSchemaValue.StringValue
        agent.ask(inputString.value).toInteractionInputValue()
    }.setPropertyWriteHandler(PROPERTY_NAME) { input, _->
        val inputInt = input.value() as DataSchemaValue.IntegerValue
        inputInt.value.toInteractionInputValue()
    }.setActionHandler(ACTION_NAME_2) { input, _->
        "10".toInteractionInputValue()
    }.setActionHandler(ACTION_NAME_3) { input, _->
        InteractionInput.Value(DataSchemaValue.NullValue)
    }.setActionHandler(ACTION_NAME_4) { _, _->
        InteractionInput.Value(DataSchemaValue.NullValue)
    }

    // Start `servient` in a separate coroutine
    val startJob = launch(Dispatchers.IO) {
        servient.start()
    }

    // Add and expose the thing after `start()` has had time to begin
    servient.addThing(exposedThing)
    servient.expose("Foo")

    // Keep the coroutine active as long as `servient.start()` is running
    startJob.join()
}