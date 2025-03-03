/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HttpProtocolClientFactoryTest {
    @Test
    fun getScheme() {
        assertEquals("http", HttpProtocolClientFactory().scheme)
    }

    @Test
    fun getClient() {
        assertIs<HttpProtocolClient>( HttpProtocolClientFactory().createClient())
    }
}