package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.form.Form
import thing.schema.VariableSchema

abstract class ThingInteraction<T> {

    abstract val description: String?

    abstract  val descriptions: Map<String, String>?

    abstract val forms: MutableList<Form>?

    abstract val uriVariables: Map<String, Map<String, VariableSchema>>?

    // Add a form and return type T
    fun addForm(form: Form): T {
        forms?.add(form)
        return this as T
    }
}
