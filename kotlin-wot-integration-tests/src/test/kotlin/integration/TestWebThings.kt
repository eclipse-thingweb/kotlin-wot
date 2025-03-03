/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.integration

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.Wot
import ai.ancf.lmos.wot.binding.http.HttpProtocolClientFactory
import ai.ancf.lmos.wot.binding.http.HttpsProtocolClientFactory
import ai.ancf.lmos.wot.credentials.BearerCredentials
import ai.ancf.lmos.wot.security.BearerSecurityScheme
import ai.ancf.lmos.wot.thing.schema.genericReadProperty
import ai.ancf.lmos.wot.thing.schema.genericWriteProperty
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class TestWebThings {

    private lateinit var wot: Wot

    @BeforeTest
    fun setup() = runTest {
        val http = HttpProtocolClientFactory()
        val https = HttpsProtocolClientFactory()
        val servient = Servient(
            clientFactories = listOf(http, https),
            credentialStore = mapOf("https://plugfest.webthings.io" to
                    BearerCredentials("dummy")
            )
        )
        wot =  Wot.create(servient)
    }

    @Test
    fun `Should control devices`() = runTest {
        val thingDescription = wot.requestThingDescription("https://plugfest.webthings.io/things/virtual-things-2",
            BearerSecurityScheme())

        val testThing = wot.consume(thingDescription)
        val status = testThing.genericReadProperty<Boolean>("on")

        println(status)

        testThing.genericWriteProperty("level", 50)
        testThing.genericWriteProperty("on", true)
    }
}