/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing

import java.util.regex.Pattern

class UriTemplate(private val template: String) {

    companion object {
        // A method to create a UriTemplate from a template string
        fun fromTemplate(template: String): UriTemplate {
            return UriTemplate(template)
        }
    }

    // Expands the URI template with the provided uriVariables map
    fun expand(uriVariables: Map<String, String>): String {
        var expandedUri = template

        // Iterate over all uriVariables and replace placeholders in the template
        uriVariables.forEach { (key, value) ->
            // Replace the placeholder {key} with the value from the uriVariables map
            expandedUri = expandedUri.replace("{$key}", value)
        }

        // Handle any remaining unresolved placeholders, for example, if a placeholder is missing a value.
        // For this example, let's throw an exception if we have unresolved placeholders
        val remainingPlaceholders = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}")
            .matcher(expandedUri)

        if (remainingPlaceholders.find()) {
            throw IllegalArgumentException("Template contains unresolved placeholders: ${remainingPlaceholders.group()}")
        }

        return expandedUri
    }

}
