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
//@Serializable(with = ContextSerializer::class)
data class Context(private val urls: MutableMap<String?, String> = HashMap()) {

    constructor(url: String) : this() {
        addContext(url)
    }

    constructor(prefix: String?, url: String) : this() {
        addContext(prefix, url)
    }

    fun addContext(url: String): Context {
        return addContext(null, url)
    }

    fun getUrl(prefix: String?): String? {
        return urls[prefix]
    }

    fun addContext(prefix: String?, url: String): Context {
        urls[prefix] = url
        return this
    }

    val defaultUrl: String?
        get() = urls[null] // Directly accessing the map

    val prefixedUrls: Map<String, String>
        get() = urls.entries
            .filter { (key, _) -> key != null }
            .associate { (key, value) -> key!! to value }
}