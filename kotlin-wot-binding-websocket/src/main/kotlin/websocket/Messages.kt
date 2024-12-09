package ai.ancf.lmos.wot.binding.websocket

import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

// Base class for all message types
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "messageType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ReadPropertyMessage::class, name = "readProperty"),
    JsonSubTypes.Type(value = WritePropertyMessage::class, name = "writeProperty"),
    JsonSubTypes.Type(value = ObservePropertyMessage::class, name = "observeProperty"),
    JsonSubTypes.Type(value = UnobservePropertyMessage::class, name = "unobserveProperty"),
    JsonSubTypes.Type(value = ReadAllPropertiesMessage::class, name = "readAllProperties"),
    JsonSubTypes.Type(value = WriteAllPropertiesMessage::class, name = "writeAllProperties"),
    JsonSubTypes.Type(value = ReadMultiplePropertiesMessage::class, name = "readMultipleProperties"),
    JsonSubTypes.Type(value = WriteMultiplePropertiesMessage::class, name = "writeMultipleProperties"),
    JsonSubTypes.Type(value = PropertyReadingMessage::class, name = "propertyReading"),
    JsonSubTypes.Type(value = PropertyReadingsMessage::class, name = "propertyReadings"),
    JsonSubTypes.Type(value = InvokeActionMessage::class, name = "invokeAction"),
    JsonSubTypes.Type(value = ActionStatusMessage::class, name = "actionStatus"),
    JsonSubTypes.Type(value = ActionStatusesMessage::class, name = "actionStatuses"),
    JsonSubTypes.Type(value = SubscribeEventMessage::class, name = "subscribeevent"),
    JsonSubTypes.Type(value = UnsubscribeEventMessage::class, name = "unsubscribeEvent"),
    JsonSubTypes.Type(value = EventMessage::class, name = "event"),
    JsonSubTypes.Type(value = ErrorMessage::class, name = "error")
)
sealed interface WoTMessage {
    var thingId: String
    var messageType: String
}

data class InvokeActionMessage(
    override var thingId: String,
    var action: String,
    var input: DataSchemaValue? = null
) : WoTMessage {
    override var messageType: String = "invokeAction"
}

// Subclass for subscribeEvent message
data class SubscribeEventMessage(
    override var thingId: String,
    var event: String,
    var lastEvent: Instant
) : WoTMessage {
    override var messageType: String = "subscribeevent"
}

// Subclass for unsubscribeEvent message
data class UnsubscribeEventMessage(
    override var thingId: String,
    var event: String
) : WoTMessage {
    override var messageType: String = "unsubscribeEvent"
}

// Subclass for event message
data class EventMessage(
    override var thingId: String,
    var event: String,
    var href: String,
    var data: DataSchemaValue,
    var timestamp: Instant = Instant.now()
) : WoTMessage {
    override var messageType: String = "event"
}

// Subclass for readProperty message
data class ReadPropertyMessage(
    override var thingId: String,
    var property: String
) : WoTMessage {
    override var messageType: String = "readProperty"
}

// Subclass for writeProperty message
data class WritePropertyMessage(
    override var thingId: String,
    var property: String,
    var data: DataSchemaValue
) : WoTMessage {
    override var messageType: String = "writeProperty"
}

// Subclass for observeAllProperties message
data class ObserveAllPropertiesMessage(
    override var thingId: String,
    var lastPropertyReading: Instant
) : WoTMessage {
    override var messageType: String = "observeAllProperties"
}

// Subclass for observeProperty message
data class ObservePropertyMessage(
    override var thingId: String,
    var property: String,
    var lastPropertyReading: Instant
) : WoTMessage {
    override var messageType: String = "observeProperty"
}

// Subclass for unobserveProperty message
data class UnobservePropertyMessage(
    override var thingId: String,
    var property: String
) : WoTMessage {
    override var messageType: String = "unobserveProperty"
}

// Subclass for readAllProperties message
data class ReadAllPropertiesMessage(
    override var thingId: String
) : WoTMessage {
    override var messageType: String = "readAllProperties"
}

// Subclass for writeAllProperties message
data class WriteAllPropertiesMessage(
    override var thingId: String,
    var data: Map<String, DataSchemaValue>
) : WoTMessage {
    override var messageType: String = "writeAllProperties"
}

// Subclass for readMultipleProperties message
data class ReadMultiplePropertiesMessage(
    override var thingId: String,
    var properties: List<String>
) : WoTMessage {
    override var messageType: String = "readMultipleProperties"
}

// Subclass for writeMultipleProperties message
data class WriteMultiplePropertiesMessage(
    override var thingId: String,
    var data: Map<String, DataSchemaValue>
) : WoTMessage {
    override var messageType: String = "writeMultipleProperties"
}

// Subclass for propertyReading message
data class PropertyReadingMessage(
    override var thingId: String,
    var property: String,
    var data: JsonNode,
    var timestamp: Instant = Instant.now()
) : WoTMessage {
    override var messageType: String = "propertyReading"
}

// Subclass for propertyReadings message
data class PropertyReadingsMessage(
    override var thingId: String,
    var data: Map<String, DataSchemaValue>,
    var timestamp: Instant = Instant.now()
) : WoTMessage {
    override var messageType: String = "propertyReadings"
}

// Subclass for error message
data class ErrorMessage(
    override var thingId: String,
    var type: String, // URI reference to the type of error
    var title: String, // Short, human-readable summary of the problem
    var status: String, // HTTP status code as a string
    var detail: String, // Detailed explanation of the error
    var instance: String // URI reference to the specific occurrence of the problem
) : WoTMessage {
    override var messageType: String = "error"
}

// Subclass for actionStatus message
data class ActionStatusMessage(
    override var thingId: String,
    var action: String,
    var status: String // Example: "pending", "completed", "failed", etc.
) : WoTMessage {
    override var messageType: String = "actionStatus"
}

// Subclass for actionStatuses message
data class ActionStatusesMessage(
    override var thingId: String,
    var statuses: Map<String, String> // Map of action names to their statuses
) : WoTMessage {
    override var messageType: String = "actionStatuses"
}