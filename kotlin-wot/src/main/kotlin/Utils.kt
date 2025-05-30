/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb

import org.eclipse.thingweb.thing.ThingDescription
import org.eclipse.thingweb.thing.schema.InteractionAffordance
import org.eclipse.thingweb.thing.schema.InteractionOptions
import org.eclipse.thingweb.thing.schema.WoTThingDescription
import org.eclipse.thingweb.thing.validateInteractionOptions
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonMapper {
    val instance: ObjectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
    }
}

fun parseInteractionOptions(
    thing: WoTThingDescription,
    ti: InteractionAffordance,
    options: InteractionOptions? = null
): InteractionOptions {

    require(validateInteractionOptions(thing, ti, options)) {
        "One or more uriVariables were not found under either '${ti.title}' Thing Interaction or '${thing.title}' Thing"
    }

    val interactionUriVariables = ti.uriVariables ?: emptyMap()
    val thingUriVariables = thing.uriVariables ?: emptyMap()
    val uriVariables = mutableMapOf<String, String>()
    options?.uriVariables?.let { userUriVariables ->
        userUriVariables.forEach { (key, value) ->
            if (key in interactionUriVariables || key in thingUriVariables) {
                uriVariables[key] = value
            }
        }
    }
    /*
    thingUriVariables.forEach { (key, value) ->
        if (key !in uriVariables && value is Map<*, *> && "default" in value) {
            uriVariables[key] = value["default"]
        }
    }
    */

    return InteractionOptions(uriVariables = uriVariables.toMap())
}

fun validateInteractionOptions(
    thingDescription: ThingDescription,
    ti: InteractionAffordance,
    options: InteractionOptions? = null
): Boolean {
    val interactionUriVariables = ti.uriVariables ?: emptyMap()
    val thingUriVariables = thingDescription.uriVariables ?: emptyMap()

    return options?.uriVariables?.all { (key, _) ->
        key in interactionUriVariables || key in thingUriVariables
    } ?: true
}