/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.spring

import org.eclipse.thingweb.Servient
import org.eclipse.thingweb.Wot
import org.eclipse.thingweb.credentials.BearerCredentials
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SpringApplicationTest {

    @Autowired
    private lateinit var credentialsProperties: CredentialsProperties

    @Autowired
    private lateinit var httpServerProperties: HttpServerProperties

    @Autowired
    private lateinit var wot: Wot

    @Autowired
    private lateinit var servient: Servient

    @Autowired
    lateinit var env: Environment

    @Test
    fun `should load http server properties from application properties`() {
        assertEquals(false, httpServerProperties.enabled)
        assertEquals(9090, httpServerProperties.port)
    }

    @Test
    fun `should initiate wot object`() {
        assertNotNull(wot)
    }

    @Test
    fun `should initiate servient object`() {
        assertContains(servient.getClientSchemes(), "https")
        assertEquals(BearerCredentials("test"), servient.credentialStore["urn:dev:wot:org:eclipse:thingweb:security-example"])
    }
}