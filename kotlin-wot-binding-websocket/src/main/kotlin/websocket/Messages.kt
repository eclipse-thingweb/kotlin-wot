/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.binding.websocket

import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant
import java.util.*

object MessageTypes {
    const val READ_PROPERTY = "readProperty"
    const val WRITE_PROPERTY = "writeProperty"
    const val OBSERVE_PROPERTY = "observeProperty"
    const val UNOBSERVE_PROPERTY = "unobserveProperty"
    const val READ_ALL_PROPERTIES = "readAllProperties"
    const val WRITE_ALL_PROPERTIES = "writeAllProperties"
    const val READ_MULTIPLE_PROPERTIES = "readMultipleProperties"
    const val WRITE_MULTIPLE_PROPERTIES = "writeMultipleProperties"
    const val PROPERTY_READING = "propertyReading"
    const val PROPERTY_READINGS = "propertyReadings"
    const val INVOKE_ACTION = "invokeAction"
    const val ACTION_STATUS = "actionStatus"
    const val ACTION_STATUSES = "actionStatuses"
    const val SUBSCRIBE_EVENT = "subscribeevent"
    const val ACKNOWLEDGEMENT = "acknowledgement"
    const val UNSUBSCRIBE_EVENT = "unsubscribeEvent"
    const val EVENT = "event"
    const val ERROR = "error"
}

// Base class for all message types
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "messageType"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ReadPropertyMessage::class, name = MessageTypes.READ_PROPERTY),
    JsonSubTypes.Type(value = WritePropertyMessage::class, name = MessageTypes.WRITE_PROPERTY),
    JsonSubTypes.Type(value = ObservePropertyMessage::class, name = MessageTypes.OBSERVE_PROPERTY),
    JsonSubTypes.Type(value = UnobservePropertyMessage::class, name = MessageTypes.UNOBSERVE_PROPERTY),
    JsonSubTypes.Type(value = ReadAllPropertiesMessage::class, name = MessageTypes.READ_ALL_PROPERTIES),
    JsonSubTypes.Type(value = WriteAllPropertiesMessage::class, name = MessageTypes.WRITE_ALL_PROPERTIES),
    JsonSubTypes.Type(value = ReadMultiplePropertiesMessage::class, name = MessageTypes.READ_MULTIPLE_PROPERTIES),
    JsonSubTypes.Type(value = WriteMultiplePropertiesMessage::class, name = MessageTypes.WRITE_MULTIPLE_PROPERTIES),
    JsonSubTypes.Type(value = PropertyReadingMessage::class, name = MessageTypes.PROPERTY_READING),
    JsonSubTypes.Type(value = PropertyReadingsMessage::class, name = MessageTypes.PROPERTY_READINGS),
    JsonSubTypes.Type(value = InvokeActionMessage::class, name = MessageTypes.INVOKE_ACTION),
    JsonSubTypes.Type(value = ActionStatusMessage::class, name = MessageTypes.ACTION_STATUS),
    JsonSubTypes.Type(value = ActionStatusesMessage::class, name = MessageTypes.ACTION_STATUSES),
    JsonSubTypes.Type(value = SubscribeEventMessage::class, name = MessageTypes.SUBSCRIBE_EVENT),
    JsonSubTypes.Type(value = UnsubscribeEventMessage::class, name = MessageTypes.UNSUBSCRIBE_EVENT),
    JsonSubTypes.Type(value = EventMessage::class, name = MessageTypes.EVENT),
    JsonSubTypes.Type(value = ErrorMessage::class, name = MessageTypes.ERROR),
    JsonSubTypes.Type(value = Acknowledgement::class, name = MessageTypes.ACKNOWLEDGEMENT)
)
sealed interface WoTMessage {
    var thingId: String
    var messageId : String
    @get:JsonIgnore // Ignore the type property in the interface itself
    val messageType: String
}

class Acknowledgement(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var message: String
) : WoTMessage {
    override val messageType: String = MessageTypes.ACKNOWLEDGEMENT
}

data class ReadPropertyMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var property: String
) : WoTMessage {
    override val messageType: String = MessageTypes.READ_PROPERTY
}

data class WritePropertyMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var property: String,
    var data: JsonNode
) : WoTMessage {
    override val messageType: String = MessageTypes.WRITE_PROPERTY
}

data class ObservePropertyMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var property: String,
    var lastPropertyReading: Instant = Instant.now()
) : WoTMessage {
    override val messageType: String = MessageTypes.OBSERVE_PROPERTY
}

data class UnobservePropertyMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var property: String
) : WoTMessage {
    override val messageType: String = MessageTypes.UNOBSERVE_PROPERTY
}

data class ReadAllPropertiesMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
) : WoTMessage {
    override val messageType: String = MessageTypes.READ_ALL_PROPERTIES
}

data class WriteAllPropertiesMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var data: Map<String, JsonNode>
) : WoTMessage {
    override val messageType: String = MessageTypes.WRITE_ALL_PROPERTIES
}

data class ReadMultiplePropertiesMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var properties: List<String>
) : WoTMessage {
    override val messageType: String = MessageTypes.READ_MULTIPLE_PROPERTIES
}

data class WriteMultiplePropertiesMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var data: Map<String, JsonNode>
) : WoTMessage {
    override val messageType: String = MessageTypes.WRITE_MULTIPLE_PROPERTIES
}

data class PropertyReadingMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var property: String,
    var data: JsonNode,
    var timestamp: Instant = Instant.now()
) : WoTMessage {
    override val messageType: String = MessageTypes.PROPERTY_READING
}

data class PropertyReadingsMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var data: Map<String, JsonNode>,
    var timestamp: Instant = Instant.now()
) : WoTMessage {
    override val messageType: String = MessageTypes.PROPERTY_READINGS
}

data class InvokeActionMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var action: String,
    var input: JsonNode? = null
) : WoTMessage {
    override val messageType: String = MessageTypes.INVOKE_ACTION
}

enum class ActionStatus(val status: String) {
    PENDING("pending"),
    COMPLETED("completed"),
    FAILED("failed");

    @JsonValue
    override fun toString(): String = status
}

data class ActionStatusMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var action: String,
    var status: ActionStatus = ActionStatus.COMPLETED, // Example: "pending", "completed", "failed"
    var output: JsonNode?
) : WoTMessage {
    override val messageType: String = MessageTypes.ACTION_STATUS
}

data class ActionStatusesMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var statuses: Map<String, String> // Map of action names to their statuses
) : WoTMessage {
    override val messageType: String = MessageTypes.ACTION_STATUSES
}

data class Subscription(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var event: String,
    var lastEvent: Instant = Instant.now()
) : WoTMessage {
    override val messageType: String = MessageTypes.SUBSCRIBE_EVENT
}

data class SubscribeEventMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var event: String,
    var lastEvent: Instant = Instant.now()
) : WoTMessage {
    override val messageType: String = MessageTypes.SUBSCRIBE_EVENT
}

data class UnsubscribeEventMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var event: String
) : WoTMessage {
    override val messageType: String = MessageTypes.UNSUBSCRIBE_EVENT
}

data class EventMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var event: String,
    var href: String? = null,
    var data: JsonNode,
    var timestamp: Instant = Instant.now()
) : WoTMessage {
    override val messageType: String = MessageTypes.EVENT
}

data class ErrorMessage(
    override var thingId: String,
    override var messageId : String = UUID.randomUUID().toString(),
    var correlationId : String? = null,
    var type: String, // URI reference to the type of error
    var title: String, // Short, human-readable summary of the problem
    var status: String, // HTTP status code as a string
    var detail: String, // Detailed explanation of the error
    var instance: String // URI reference to the specific occurrence of the problem
) : WoTMessage {
    override val messageType: String = MessageTypes.ERROR
}
