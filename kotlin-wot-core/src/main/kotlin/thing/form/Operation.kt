package ai.ancf.lmos.wot.thing.form

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue


enum class Operation(private val tdValue: String) {
    // properties
    READ_PROPERTY("readproperty"),
    WRITE_PROPERTY("writeproperty"),
    OBSERVE_PROPERTY("observeproperty"),
    UNOBSERVE_PROPERTY("unobserveproperty"),
    READ_ALL_PROPERTIES("readallproperty"),
    READ_MULTIPLE_PROPERTIES("readmultipleproperty"),

    // events
    SUBSCRIBE_EVENT("subscribeevent"),
    UNSUBSCRIBE_EVENT("unsubscribeevent"),

    // actions
    INVOKE_ACTION("invokeaction");

    @JsonValue
    private fun toJsonValue(): String {
        return tdValue
    }

    companion object {
        private val LOOKUP: MutableMap<String, Operation> = HashMap()

        init {
            for (env in entries) {
                LOOKUP[env.toJsonValue()] = env
            }
        }

        @JsonCreator
        fun fromJsonValue(jsonValue: String): Operation? {
            return LOOKUP[jsonValue]
        }
    }
}
