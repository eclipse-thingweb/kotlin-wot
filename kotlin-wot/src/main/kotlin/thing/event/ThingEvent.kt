/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.event

import org.eclipse.thingweb.thing.schema.Type
import org.eclipse.thingweb.thing.form.Form
import org.eclipse.thingweb.thing.schema.DataSchema
import org.eclipse.thingweb.thing.schema.EventAffordance
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ThingEvent<T, S, C>(
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,

    @JsonProperty("@type")
    @JsonInclude(NON_NULL)
    override var objectType: Type? = null,

    @JsonInclude(NON_NULL)
    override var data: DataSchema<T>? = null,

    @JsonInclude(NON_EMPTY)
    override var description: String? = null,

    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var uriVariables: MutableMap<String, DataSchema<Any>>? = null,

    @JsonInclude(NON_EMPTY)
    override var forms: MutableList<Form> = mutableListOf(),

    @JsonInclude(NON_EMPTY)
    override var subscription: DataSchema<S>? = null,

    @JsonInclude(NON_EMPTY)
    override var cancellation: DataSchema<C>? = null,

    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null

) : EventAffordance<T, S, C>
