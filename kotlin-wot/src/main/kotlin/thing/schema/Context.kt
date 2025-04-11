/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.schema

import org.eclipse.thingweb.thing.ContextDeserializer
import org.eclipse.thingweb.thing.ContextSerializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * Represents a JSON-LD context.
 */
@JsonDeserialize(using = ContextDeserializer::class)
@JsonSerialize(using = ContextSerializer::class)
data class Context(val defaultUrls: MutableList<String> = mutableListOf(), private val prefixeUrls: MutableMap<String, String> = HashMap()) {

    constructor(url: String) : this() {
        addContext(url)
    }

    constructor(prefix: String, url: String) : this() {
        addContext(prefix, url)
    }

    fun addContext(url: String): Context {
        defaultUrls.add(url)
        return this
    }

    fun addContext(prefix: String, url: String): Context {
        prefixeUrls[prefix] = url
        return this
    }

    val prefixedUrls: Map<String, String>
        get() = prefixeUrls.entries
            .filter { (key, _) -> key != null }
            .associate { (key, value) -> key!! to value }
}