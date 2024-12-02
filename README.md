# kotlin-wot: A Framework for implementing Web of Things in Kotlin

**kotlin-wot** is a framework designed to enable developers to implement [**Web of Things (WoT)**](https://www.w3.org/WoT/) servers and clients in Kotlin. Built from the ground up with Kotlin and leveraging modern coroutine-based architecture, it aims to provide a **fast, reliable, and extensible framework for AI or IoT applications**. By abstracting low-level details and protocols through the use of [**Thing Descriptions (TDs)**](https://www.w3.org/TR/wot-thing-description11/), kotlin-wot empowers developers to focus on creating business logic.
Thing Descriptions provide an excellent alternative to OpenAPI or AsyncAPI. Unlike these formats, Thing Descriptions are protocol-agnostic and utilize forms to remain independent of specific transport protocols, enabling greater flexibility and interoperability across diverse ecosystems.

The implementation was inspired by the awesome [Eclipse Thingweb](https://thingweb.io/) and [node-wot](https://github.com/eclipse-thingweb/node-wot). There are also open-source implementations available for TypeScript, Dart, Rust and Python.

NOTE: The library is still under development

## Web of Things Principles in a Nutshell

The [**Web of Things (WoT)**](https://www.w3.org/WoT/) architecture bridges the gap between diverse technologies in AI, IoT, and traditional IT systems by leveraging and extending standardized web technologies. It introduces a unifying layer of abstraction that encapsulates agents, devices, services, and protocols, simplifying their integration and interaction. At its core, WoT employs the Thing Description (TD), a metadata-rich JSON-LD document that describes a Things's properties, actions, and events, along with protocol-specific details (forms) for seamless interaction. 

WoT is protocol-agnostic, supporting a wide array of communication standards (e.g., HTTP, MQTT, CoAP). By aligning with existing web standards, such as JSON, JSON Schema and Linked Data, WoT enables developers to create interoperable AI/IoT applications. WoT’s adaptable framework is designed to address diverse use cases—from AI agent systems, smart homes to industrial IoT — ensuring that the architecture works across domains.

At its core, the WoT defines an **information model** for describing Things and Services, including how to interact with them. This model is encapsulated in the **Thing Description (TD)**, a JSON-LD document that outlines the following:

- Metadata about the Thing
- The Thing’s **capabilities** (properties, actions, and events)
- Its network services (APIs)
- Security definitions
- Web links to related Things or resources

## Thing Description (TD)

The [**Thing Description (TD)**](https://www.w3.org/TR/wot-thing-description11/) is a standardized metadata format used to describe a Thing’s structure and interactions. Each TD is a machine-readable document that defines how to communicate with a Thing. kotlin-wot uses the TD abstraction to support developers in creating applications quickly and transport protocol-agnostic.

## Thing Capabilities (Affordances)

Every Thing in kotlin-wot is modeled with the following capabilities, known as **affordances**:

### ⚙️ **Properties**
A **property** represents a value that can be read, written, or observed. For example:
- A **temperature sensor** may have a property that holds the current temperature.
- An **AI chatbot** could have a property `modelConfiguration`, which shows the current configuration of an LLM model.
- An **AI recommendation engine** might have a property `activeRecommendations` listing the recommendations being served.

### 🦾 **Actions**
An **action** represents an operation that can be invoked. For example:
- A **smart light bulb** may have an action to turn it on or off.
- A **chatbot** could have an action `generateReply(input: String)` to respond based on user input.
- An **AI scheduling assistant** could provide an action `scheduleMeeting(date: String, participants: List<String>)` to book a meeting.

### ⚡ **Events**
An **event** is a notification triggered by a specific occurrence. For example:
- A **motion sensor** may send an event notification when motion is detected.
- A **chatbot** might emit an event `conversationEnded` when the user ends the chat session.
- An **AI monitoring system** might trigger an event `anomalyDetected` when it identifies unusual behavior in a monitored system.

### Example of a Thing Description

This example illustrates how a Weather Agent can be modeled using a Thing Description, with HTTP as the primary communication protocol, although alternative protocols may also be utilized. The Agent metadata describes that the agent uses the gpt-4o model from Azure and integrates with OpenWeatherMap API to provide weather information. The agent supports both text and voice interactions in English and German, adheres to GDPR compliance, and uses data anonymization. It offers a single action, "getWeather," which takes a natural language question and interaction mode as input and returns weather information in natural language. The service is secured using basic authentication and is accessed via a POST request to a specified endpoint, but other security schemes, such as OAuth2 tokens, can also be used.

```json
{
    "@context": [
        "https://www.w3.org/2022/wot/td/v1.1",
        {
            "htv": "http://www.w3.org/2011/http#",
            "ex": "https://weatherai.example.com",
        },
        "https://schema.org/"
    ],
    "id": "urn:uuid:6f1d3a7a-1f97-4e6b-b45f-f3c2e1c84c77",
    "title": "WeatherAgent",
    "@type": "ex:Agent",
    "links": [{
      "rel": "service-doc",
      "href": "https://weatherai.example.com/manual.pdf",
      "type": "application/pdf",
      "hreflang": "en"
    }],
    "ex:metadata": {
        "ex:vendor": {
            "ex:name": "WeatherAI Inc.",
            "ex:url": "https://weatherai.example.com"
        },
        "ex:model": {
            "ex:name": "gpt-4o",
            "ex:provider": "Azure"
        },
        "ex:serviceIntegration": {
            "ex:weatherAPI": "OpenWeatherMap",
            "ex:apiVersion": "v2.5",
            "ex:apiDocumentation": "https://openweathermap.org/api"
        },
        "ex:dataPrivacy": {
            "ex:dataRetentionPeriod": "30 days",
            "ex:anonymizationMethod": "HASHING"
        },
        "ex:interaction": {
            "ex:supportedLanguages": ["en_US", "de_DE"],
            "ex:interactionMode": ["text", "voice"]
        },
        "ex:compliance": {
            "ex:regulatoryCompliance": "GDPR"
        }
    },
    "securityDefinitions": {
        "basic_sc": {
            "scheme": "basic",
            "in": "header"
        }
    },
    "security": "basic_sc",
    "actions": {
        "getWeather": {
            "description": "Fetches weather information based on user input.",
            "safe": true,
            "idempotent": false,
            "synchronous": true,
            "input": {
               "type": "object",
                "properties": {
                    "question": {
                        "type": "string"
                    },
                    "interactionMode": {
                        "type": "string",
                        "enum": ["text", "voice"]
                    }
                },
                "required": ["question","interactionMode"]
            },
            "output": {
                "type": "string",
                "description": "Natural language output providing weather information."
            },            
            "forms": [
                {
                    "op": "invokeaction",
                    "href": "https://weatherai.example.com/weather",
                    "contentType": "application/json",
                    "htv:methodName":"POST"
                }
            ]
        }
    }
}
```

## Advantages of kotlin-wot

1. **Native Kotlin Implementation:**
    - Built with Kotlin, leveraging coroutine-based concurrency for seamless asynchronous programming.
2. **Abstracted Protocol Handling:**
    - Developers can focus on business logic without worrying about low-level communication protocols.
3. **Standards-Compliant:**
    - Fully adheres to the W3C WoT specifications, ensuring interoperability and reusability.
4. **Extensibility:**
    - Easy to extend to support more protocols

## Example Thing

The `SimpleThing` class defines a Web of Things (WoT) model with properties, actions, and events using annotations.  This structure allows external systems to interact with the Thing's state, invoke functionality, and subscribe to real-time notifications, all described in a Thing Description (TD), making it a flexible and extensible component for AI/IoT applications.

One of the key benefits of the Web of Things (WoT) framework is that developers can focus on building the core functionality of their applications without needing to delve into the low-level details of communication protocols like MQTT, WebSockets, or AMQP. By abstracting these protocols, kotlin-wot allows developers to use higher-level constructs such as coroutines and flows for managing asynchronous behavior and real-time interactions. With coroutines, developers can write non-blocking, concurrent code in a sequential and readable manner, simplifying the development of complex workflows. Flows, on the other hand, provide a powerful way to handle streams of data that can be emitted over time, making it easier to work with dynamic or event-driven environments. This abstraction minimizes the need for developers to manage protocol-specific intricacies and allows them to focus on implementing the logic and behavior of Things, enabling faster and more intuitive development.

```kotlin
@Thing(
    id = "simpleThing",
    title = "Simple Thing",
    description = "A thing with complex properties, actions, and events."
)
@VersionInfo(instance = "1.0.0")
class SimpleThing {


    @Property(name = "observableProperty", title = "Observable Property", readOnly = true)
    val observableProperty : MutableStateFlow<String> = MutableStateFlow("Hello World")

    @Property(name = "mutableProperty")
    var mutableProperty: String = "test"

    @Property(name = "readyOnlyProperty", readOnly = true)
    val readyOnlyProperty: String = "test"

    @Property(name = "writeOnlyProperty", writeOnly = true)
    var writeOnlyProperty: String = "test"

    @Action(name = "inOutAction")
    fun inOutAction(input : String) : String {
        return "$input output"
    }

    @Event(name = "statusUpdated")
    fun statusUpdated(): Flow<String> {
        return flow {
            emit("Status updated")
        }
    }
}
```

In **kotlin-wot**, you can easily configure the protocols through which a **Thing** should be exposed by specifying the appropriate protocol servers when creating the `Servient`. The `Servient` acts as the core orchestrator, managing both the exposure and interaction of Things across various protocols.

### Configuring Protocols for Exposure

In the example provided:

```kotlin
val mqttConfig = MqttClientConfig("localhost", 61890, "wotServer")
val servient = Servient(
    servers = listOf(HttpProtocolServer(), MqttProtocolServer(mqttConfig)),
    clientFactories = listOf(HttpProtocolClientFactory())
)
```

Here’s what is happening:

1. **`HttpProtocolServer`**: Enables HTTP-based interaction with the Thing.
2. **`MqttProtocolServer`**: Configured with the MQTT broker details (`localhost:61890`) to enable MQTT-based communication.
3. **`clientFactories`**: Specifies client protocols that the `Servient` can use to interact with other Things, such as HTTP clients.

This flexible design allows you to mix and match protocols by adding or removing protocol servers in the configuration. For example, if you want only MQTT exposure, simply include `MqttProtocolServer` and omit the others.

### Automatic Thing Description Generation

The `ThingDescription (TD)` is **automatically generated** based on the class passed to the `ExposedThingBuilder`. This greatly simplifies the process of defining a Thing, as the framework introspects the provided class and maps its properties, actions, and events to the standard WoT affordances:

- **Properties** are derived from readable or writable fields.
- **Actions** are identified from callable methods.
- **Events** can be generated and configured using Kotlin flows or reactive streams.

```kotlin
val wot = Wot.create(servient)
val exposedThing = ExposedThingBuilder.createExposedThing(wot, agent, ThingAgent::class)
```

Here, the `ThingAgent` class is analyzed, and its capabilities (e.g., properties, actions, and events) are automatically included in the generated TD. Developers don’t need to write JSON-LD manually—the framework takes care of this, ensuring compliance with the WoT standard.

### Dynamic Exposure

After configuring the `Servient` and defining the Thing, it can be dynamically added and exposed:

```kotlin
servient.addThing(exposedThing as WoTExposedThing)
servient.expose("agent")
```

- The `addThing` method registers the Thing with the servient.
- The `expose` method starts exposing the Thing over the configured protocols, making it accessible via HTTP and MQTT in this case.

### Key Benefits

- **Protocol Flexibility**: Effortlessly configure and support multiple protocols for Thing exposure.
- **Automatic TD Creation**: Save time and reduce errors with automatically generated Thing Descriptions.
- **Standards Compliance**: Ensures all Things are described in a standardized format, promoting interoperability.

With kotlin-wot, developers can focus on implementing business logic while the framework handles the complexities of protocol management and Thing Description generation.

## Step-by-Step Guide to Consuming a Thing and Interacting with It

Here’s how to consume a Thing and interact with its properties and actions in a Web of Things (WoT) setup. Below are the main steps extracted from the provided code:

###  Create a WoT Object

Example:
   ```kotlin
  // Create the WoT object which can make use of HTTP. You can also add other protocols.
   val wot = Wot.create(Servient(clientFactories = listOf(HttpProtocolClientFactory()))) 
   ```

###  **Obtain the Thing Description**
- The first step in interacting with a Thing is to obtain its **Thing Description (TD)**, which describes the capabilities of the Thing (such as properties, actions, and events).
- Use the `wot.requestThingDescription` function to fetch the TD of a Thing by its URL.

Example:
   ```kotlin
   val thingDescription = wot.requestThingDescription("http://localhost:8080/${thingId}")
   ```

### **Consume the Thing**
- Once you have the TD, you can consume the Thing using the `wot.consume` method. This will allow you to interact with its properties and actions.
- The `consume` function returns a `ConsumedThing` object, which represents the Thing in your code and provides methods to interact with it.

Example:
   ```kotlin
   val consumedThing = wot.consume(thingDescription)
   ```

### **Read a Property**
- To interact with a property of the Thing, you can call the `readProperty` method on the `ConsumedThing` object.
- This will return the current value of the property, which can be cast to the appropriate data type.

Example:
   ```kotlin
   val readProperty = consumedThing.readProperty("mutableProperty")
   ```

### **Write to a Property**
- To modify the value of a property, you can use the `writeProperty` method on the `ConsumedThing` object.
- You need to pass the updated value to this method. Ensure that the value is wrapped in the correct `InteractionInput.Value` type.

Example:
   ```kotlin
   consumedThing.writeProperty("mutableProperty", 20.toInteractionInputValue()) // Update the property value
   ```

### **Invoke an Action**
- If the Thing exposes any actions, you can invoke them by calling the `invokeAction` method on the `ConsumedThing` object.
- You need to provide the action name and any necessary input parameters.

Example:
   ```kotlin
   val output = consumedThing.invokeAction("inOutAction", "actionInput".toInteractionInputValue())
   ```

### **Read All Properties**
- You can read all the properties of the Thing at once using the `readAllProperties` method on the `ConsumedThing` object.
- This method returns a map of property names to their respective values.

Example:
   ```kotlin
   val responseMap = consumedThing.readAllProperties()
   ```

### **Observe Property Changes**
- If the Thing supports property observation, you can use the `observeProperty` method to listen for updates to a property.
- When a property changes, the listener will be triggered with the new value.

Example:
   ```kotlin
   consumedThing.observeProperty("observableProperty", listener = { println("Property observed: $it") })
   ```

### **Subscribe to Events**
- If the Thing supports event subscription, you can use the `subscribeEvent` method to listen for events.
- When an event is emitted, the listener will be triggered with the new value.

Example:
   ```kotlin
   consumedThing.subscribeEvent("statusUpdated", listener = { println("Event received: $it") })
   ```

## Thing Description Discovery

W3C Web of Things (WoT) offers a mechanism that things can propagate metadata using protocols like mDNS for local discovery and/or can register themselves on centralized directories for broader access. [W3C Web of Things (WoT) Discovery](https://www.w3.org/TR/wot-discovery/#architecture) describes how things can register themselves in a central directory, known as a Thing Description Directory (TDD), through a process that involves several steps:

For more details, refer to the official [W3C Web of Things](https://www.w3.org/WoT/) website.



