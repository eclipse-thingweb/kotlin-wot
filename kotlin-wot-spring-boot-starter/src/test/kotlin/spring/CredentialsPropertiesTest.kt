/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package spring

import ai.ancf.lmos.wot.credentials.ApiKeyCredentials
import ai.ancf.lmos.wot.credentials.BasicCredentials
import ai.ancf.lmos.wot.credentials.BearerCredentials
import ai.ancf.lmos.wot.spring.Credentials
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CredentialsPropertiesTest {

    @Test
    fun `test convert with bearer credentials`() {
        val credentials = Credentials(type = "bearer", token = "sample_token")

        val convertedCredentials = credentials.convert()

        // Ensure the converted credentials is of type BearerCredentials and the token matches
        assertTrue(convertedCredentials is BearerCredentials)
        assertEquals("sample_token", (convertedCredentials as BearerCredentials).token)
    }

    @Test
    fun `test convert with basic credentials`() {
        val credentials = Credentials(type = "basic", username = "user", password = "pass")

        val convertedCredentials = credentials.convert()

        // Ensure the converted credentials is of type BasicCredentials
        assertTrue(convertedCredentials is BasicCredentials)
        val basicCredentials = convertedCredentials as BasicCredentials
        assertEquals("user", basicCredentials.username)
        assertEquals("pass", basicCredentials.password)
    }

    @Test
    fun `test convert with API key credentials`() {
        val credentials = Credentials(type = "apikey", apiKey = "sample_api_key")

        val convertedCredentials = credentials.convert()

        // Ensure the converted credentials is of type ApiKeyCredentials
        assertTrue(convertedCredentials is ApiKeyCredentials)
        assertEquals("sample_api_key", (convertedCredentials as ApiKeyCredentials).apiKey)
    }

    @Test
    fun `test convert with missing token for bearer credentials`() {
        val credentials = Credentials(type = "bearer")

        val exception = assertFailsWith<IllegalArgumentException> {
            credentials.convert()
        }

        assertEquals("Token is required for bearer credentials", exception.message)
    }

    @Test
    fun `test convert with missing username and password for basic credentials`() {
        val credentials = Credentials(type = "basic")

        val exception = assertFailsWith<IllegalArgumentException> {
            credentials.convert()
        }

        assertEquals("Username and password are required for basic credentials", exception.message)
    }

    @Test
    fun `test convert with missing apiKey for apiKey credentials`() {
        val credentials = Credentials(type = "apikey")

        val exception = assertFailsWith<IllegalArgumentException> {
            credentials.convert()
        }

        assertEquals("API Key is required for API Key credentials", exception.message)
    }

    @Test
    fun `test convert with unknown credential type`() {
        val credentials = Credentials(type = "unknown")

        val exception = assertFailsWith<IllegalArgumentException> {
            credentials.convert()
        }

        assertEquals("Unknown credentials type: unknown", exception.message)
    }
}