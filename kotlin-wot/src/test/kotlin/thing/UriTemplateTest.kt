/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UriTemplateTest {

    @Test
    fun expandReplacesPlaceholdersWithValues() {
        val template = UriTemplate.fromTemplate("/things/{thingId}/properties/{propertyName}")
        val uriVariables = mapOf("thingId" to "123", "propertyName" to "456")
        val result = template.expand(uriVariables)
        assertEquals("/things/123/properties/456", result)
    }

    @Test
    fun expandThrowsExceptionForUnresolvedPlaceholders() {
        val template = UriTemplate.fromTemplate("/things/{thingId}/properties/{propertyName}")
        val uriVariables = mapOf("thingId" to "123")
        val exception = assertFailsWith<IllegalArgumentException> {
            template.expand(uriVariables)
        }
        assertTrue(exception.message!!.contains("unresolved placeholders"))
    }

    @Test
    fun expandHandlesEmptyTemplate() {
        val template = UriTemplate.fromTemplate("")
        val uriVariables = mapOf("thingId" to "123")
        val result = template.expand(uriVariables)
        assertEquals("", result)
    }

    @Test
    fun expandHandlesNoPlaceholders() {
        val template = UriTemplate.fromTemplate("/things/properties/all")
        val uriVariables = mapOf("thingId" to "123")
        val result = template.expand(uriVariables)
        assertEquals("/things/properties/all", result)
    }

    @Test
    fun expandHandlesEmptyUriVariables() {
        val template = UriTemplate.fromTemplate("/things/{thingId}/properties/{propertyName}")
        val uriVariables = emptyMap<String, String>()
        val exception = assertFailsWith<IllegalArgumentException> {
            template.expand(uriVariables)
        }
        assertTrue(exception.message!!.contains("unresolved placeholders"))
    }

    @Test
    fun expandHandlesQueryVariablesInPath() {
        val template = UriTemplate.fromTemplate("/search?query={query}&page={page}")
        val uriVariables = mapOf("query" to "kotlin", "page" to "1")
        val result = template.expand(uriVariables)
        assertEquals("/search?query=kotlin&page=1", result)
    }
}