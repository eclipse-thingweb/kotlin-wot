package ai.ancf.lmos.wot.thing.property

import ai.ancf.lmos.wot.schema.DataSchema
import ai.ancf.lmos.wot.schema.VariableDataSchema
import ai.ancf.lmos.wot.thing.ThingInteraction
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

abstract class ThingProperty<T>(

    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val objectType: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    override val type: String = "string",

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val isObservable: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val isReadOnly: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val isWriteOnly: Boolean = false,

    @JsonAnyGetter
    val optionalProperties: Map<String, Any> = emptyMap()

) : ThingInteraction<ThingProperty<T>?>(), DataSchema<T> {

    override val classType: Class<T>
        get() = VariableDataSchema.Builder()
            .setType(type)
            .build()
            .classType

    fun getOptional(name: String): Any? {
        return optionalProperties[name]
    }
}

