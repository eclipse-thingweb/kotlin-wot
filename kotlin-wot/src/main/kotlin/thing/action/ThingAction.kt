/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.action

import org.eclipse.thingweb.thing.schema.Type
import org.eclipse.thingweb.thing.form.Form
import org.eclipse.thingweb.thing.schema.ActionAffordance
import org.eclipse.thingweb.thing.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonProperty

data class ThingAction<I: Any, O: Any>(
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,

    @JsonInclude(NON_EMPTY)
    override var description: String? = null,

    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var uriVariables: MutableMap<String, DataSchema<Any>>? = null,

    @JsonInclude(NON_EMPTY)
    override var forms: MutableList<Form> = mutableListOf(),

    @JsonProperty("@type")
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,

    @JsonInclude(NON_NULL)
    override var input: DataSchema<I>? = null,

    @JsonInclude(NON_NULL)
    override var output: DataSchema<O>? = null,

    @JsonInclude(NON_DEFAULT)
    override var safe: Boolean = false,

    @JsonInclude(NON_DEFAULT)
    override var idempotent: Boolean = false,

    @JsonInclude(NON_NULL)
    override var synchronous: Boolean? = null,

    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null
) : ActionAffordance<I, O> {
}

