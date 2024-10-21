package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.form.Form
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

abstract class ThingInteraction<T>(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val description: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val descriptions: Map<String, String>? = null,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val forms: MutableList<Form>? = mutableListOf(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val uriVariables: Map<String, Map<String, Any>>? = emptyMap()
) {

    // Add a form and return type T
    fun addForm(form: Form): T {
        forms?.add(form)
        return this as T
    }

    override fun hashCode(): Int {
        return Objects.hash(description, descriptions, forms, uriVariables)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ThingInteraction<*>) return false
        val that = o
        return description == that.description &&
                descriptions == that.descriptions &&
                forms == that.forms &&
                uriVariables == that.uriVariables
    }
}
