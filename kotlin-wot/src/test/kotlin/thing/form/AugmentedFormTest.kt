/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing.form

import AugmentedForm
import ai.ancf.lmos.wot.security.BasicSecurityScheme
import ai.ancf.lmos.wot.thing.schema.WoTForm
import ai.ancf.lmos.wot.thing.schema.WoTThingDescription
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class AugmentedFormTest {

    private val mockForm = mockk<WoTForm> {
        every { href } returns "/resource/{id}"
        every { security } returns listOf("basic")
    }

    private val mockThingDescription = mockk<WoTThingDescription> {
        every { base } returns "https://example.com/api/"
        every { securityDefinitions } returns mutableMapOf(
            "basic" to BasicSecurityScheme("BasicAuth")
        )
    }

    @Test
    fun `href should resolve correctly with base`() {
        val augmentedForm = AugmentedForm(mockForm, mockThingDescription)

        val expectedHref = "https://example.com/api/resource/{id}"
        assertEquals(expectedHref, augmentedForm.href)
    }

    @Test
    fun `securityDefinitions should be mapped correctly`() {
        val augmentedForm = AugmentedForm(mockForm, mockThingDescription)

        assertEquals(1, augmentedForm.securityDefinitions.size)
    }

}