package ai.ancf.lmos.wot.thing.event

import ai.ancf.lmos.wot.schema.DataSchema
import ai.ancf.lmos.wot.schema.VariableDataSchema
import ai.ancf.lmos.wot.thing.ThingInteraction
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*


@JsonIgnoreProperties(ignoreUnknown = true)
open class ThingEvent<T>(
    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val objectType: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(`as` = VariableDataSchema::class)
    val data: DataSchema<T>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val type: String? = null
) : ThingInteraction<ThingEvent<T>?>() {

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), objectType, data, type)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ThingEvent<*>) return false
        if (!super.equals(o)) return false

        return objectType == o.objectType && data == o.data && type == o.type
    }

    override fun toString(): String {
        return "ThingEvent(objectType=$objectType, data=$data, type=$type, " +
                "description='$description', descriptions=$descriptions, " +
                "forms=$forms, uriVariables=$uriVariables)"
    }
}
