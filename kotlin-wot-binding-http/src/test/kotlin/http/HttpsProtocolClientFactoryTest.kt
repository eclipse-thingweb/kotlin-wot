/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HttpsProtocolClientFactoryTest {

    @Test
    fun getScheme() {
        assertEquals("https", HttpsProtocolClientFactory().scheme)
    }

    @Test
    fun getClient() {
        assertIs<HttpProtocolClient>( HttpsProtocolClientFactory().createClient())
    }
}