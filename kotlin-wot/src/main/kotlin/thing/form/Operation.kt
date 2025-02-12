package ai.ancf.lmos.wot.thing.form

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Enumeration representing different operation types for Property, Action, and Event Affordances.
 */
enum class Operation(private val tdValue: String) {
    // Properties
    READ_PROPERTY("readproperty"),
    WRITE_PROPERTY("writeproperty"),
    OBSERVE_PROPERTY("observeproperty"),
    UNOBSERVE_PROPERTY("unobserveproperty"),
    OBSERVE_ALL_PROPERTIES("observeallproperties"),
    UNOBSERVE_ALL_PROPERTIES("unobserveallproperties"),
    READ_ALL_PROPERTIES("readallproperties"),
    WRITE_ALL_PROPERTIES("writeallproperties"),
    READ_MULTIPLE_PROPERTIES("readmultipleproperties"),
    WRITE_MULTIPLE_PROPERTIES("writemultipleproperties"),

    // Events
    SUBSCRIBE_EVENT("subscribeevent"),
    UNSUBSCRIBE_EVENT("unsubscribeevent"),

    // Actions
    INVOKE_ACTION("invokeaction"),
    QUERY_ACTION("queryaction"),
    CANCEL_ACTION("cancelaction");

    @JsonValue
    fun toJsonValue(): String {
        return tdValue
    }

    companion object {
        private val LOOKUP: MutableMap<String, Operation> = HashMap()

        init {
            for (operation in entries) {
                LOOKUP[operation.toJsonValue()] = operation
            }
        }

        @JsonCreator
        fun fromJsonValue(jsonValue: String): Operation? {
            return LOOKUP[jsonValue]
        }
    }
}
