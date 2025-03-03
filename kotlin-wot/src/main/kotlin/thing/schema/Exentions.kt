/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.schema

import org.eclipse.thingweb.JsonMapper
import com.fasterxml.jackson.databind.JsonNode

// Extension function for creating an InteractionInput.Value from a string
fun String.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(JsonMapper.instance.valueToTree((this)))
}

fun MutableMap<*, *>.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(JsonMapper.instance.valueToTree((this)))
}

fun MutableList<*>.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(JsonMapper.instance.valueToTree((this)))
}

fun Boolean.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(JsonMapper.instance.valueToTree((this)))
}

fun String.toDataSchemeValue(): JsonNode {
    return JsonMapper.instance.valueToTree((this))
}


fun Boolean.toDataSchemeValue(): JsonNode {
    return JsonMapper.instance.valueToTree((this))
}


fun Number.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(JsonMapper.instance.valueToTree((this)))
}
fun Number.toDataSchemeValue(): JsonNode {
    return JsonMapper.instance.valueToTree((this))
}

fun Int.toInteractionInputValue(): InteractionInput.Value {
    return InteractionInput.Value(JsonMapper.instance.valueToTree((this)))
}

fun Int.toDataSchemeValue(): JsonNode {
    return JsonMapper.instance.valueToTree((this))
}

fun List<*>.toDataSchemeValue(): JsonNode {
    return JsonMapper.instance.valueToTree((this))
}
