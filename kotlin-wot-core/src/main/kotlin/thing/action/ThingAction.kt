package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.schema.DataSchema
import ai.ancf.lmos.wot.schema.VariableDataSchema
import ai.ancf.lmos.wot.thing.ThingInteraction
import ai.ancf.lmos.wot.thing.form.Form
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*

open class ThingAction<I, O>(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    description: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    descriptions: Map<String, String>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    uriVariables: Map<String, Map<String, Any>>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    forms: MutableList<Form>?,

    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val objectType: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(`as` = VariableDataSchema::class)
    val input: DataSchema<I>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(`as` = VariableDataSchema::class)
    val output: DataSchema<O>? = null

) : ThingInteraction<ThingAction<I, O>?>(description, descriptions, forms, uriVariables) {


    fun getInput(): DataSchema<I>? = input

    fun getOutput(): DataSchema<O>? = output

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), objectType, input, output)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ThingAction<*, *>) return false
        if (!super.equals(o)) return false
        return objectType == o.objectType && input == o.input && output == o.output
    }

    override fun toString(): String {
        return "ThingAction{" +
                "objectType='$objectType', " +
                "input=$input, " +
                "output=$output, " +
                "description='$description', " +
                "descriptions=$descriptions, " +
                "forms=$forms, " +
                "uriVariables=$uriVariables" +
                '}'
    }
}
