/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.InteractionAffordance
import ai.ancf.lmos.wot.thing.schema.PropertyAffordance
import java.net.URI

// Find a form matching the URI scheme, request URL, and optionally content type

fun findRequestMatchingFormIndex(
    forms: List<Form>?,
    uriScheme: String,
    requestUrl: String?,
    contentType: String? = null
): Int {
    if (forms == null) return 0

    // First, find forms with matching URL protocol and path
    var matchingForms: List<Form> = forms.filter { form ->
        // Remove optional URI variables from href
        val formUrl = URI(form.href.replace(Regex("\\{[\\S]*\\}"), ""))

        // Remove URI variables from the request URL, if any
        val reqUrl = requestUrl?.takeIf { it.contains("?").not() }

        formUrl.scheme == uriScheme && (reqUrl == null || formUrl.path == reqUrl)
    }

    // Optionally try to match form's content type to the request's one
    if (contentType != null) {
        matchingForms = matchingForms.filter { form -> form.contentType == contentType }
    }

    return if (matchingForms.isNotEmpty()) forms.indexOf(matchingForms[0]) else 0
}

// Get the form index for a particular operation


fun getFormIndexForOperation(
    interaction: InteractionAffordance,
    type: String,
    operation: Operation? = null,
    formIndex: Int? = null
): Int {
    var finalFormIndex = -1

    // Default operations based on the type
    val defaultOps = mutableListOf<Operation>()
    when (type) {
        "property" -> {
            interaction as PropertyAffordance<*>
            if (interaction.readOnly && operation == Operation.WRITE_PROPERTY || interaction.writeOnly && operation == Operation.READ_PROPERTY) return finalFormIndex

            if (!interaction.readOnly) defaultOps.add(Operation.WRITE_PROPERTY)
            if (!interaction.writeOnly) defaultOps.add(Operation.READ_PROPERTY)
        }
        "action" -> defaultOps.add(Operation.INVOKE_ACTION)
        "event" -> defaultOps.addAll(listOf(Operation.SUBSCRIBE_EVENT, Operation.UNSUBSCRIBE_EVENT))
    }

    // If a form index is given, check if it's valid
    if (formIndex != null && interaction.forms.size > formIndex) {
        val form = interaction.forms[formIndex]
        if (operation == null || form.op?.contains(operation) == true) {
            finalFormIndex = formIndex
        }
    }

    // Loop through all forms if no form was found yet
    if (finalFormIndex == -1) {
        if (operation != null) {
            interaction.forms.forEachIndexed { index, form ->
                if (form.op?.contains(operation) == true) {
                    finalFormIndex = index
                    return@forEachIndexed
                }
            }
        } else {
            interaction.forms.forEachIndexed { index, _ ->
                finalFormIndex = index
                return@forEachIndexed
            }
        }
    }

    // Return the final form index
    return finalFormIndex
}

// Get possible operation values for a property
fun getPropertyOpValues(property: PropertyAffordance<*>): List<Operation> {
    val op = mutableListOf<Operation>()

    if (!property.readOnly) {
        op.add(Operation.WRITE_PROPERTY)
    }

    if (!property.writeOnly) {
        op.add(Operation.READ_PROPERTY)
    }

    if (op.isEmpty()) {
        println("Warning: Property was declared both as readOnly and writeOnly.")
    }

    if (property.observable) {
        op.add(Operation.OBSERVE_PROPERTY)
        op.add(Operation.UNOBSERVE_PROPERTY)
    }
    return op
}
