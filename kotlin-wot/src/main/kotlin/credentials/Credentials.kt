/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.credentials

import org.eclipse.thingweb.thing.schema.WoTForm
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = BasicCredentials::class, name = "basic"),
    JsonSubTypes.Type(value = BearerCredentials::class, name = "bearer"),
    JsonSubTypes.Type(value = ApiKeyCredentials::class, name = "apikey")
)
interface Credentials {
}

fun interface CredentialsProvider {
    fun getCredentials(form : WoTForm): Credentials?
}

data class BearerCredentials(
    val token: String
) : Credentials{
    override fun toString(): String {
        return "Bearer $token"
    }
}

data class BasicCredentials(
    val username: String,
    val password: String
) : Credentials {
    override fun toString(): String {
        val basicAuth = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        return "Basic $basicAuth"
    }
}

data class ApiKeyCredentials(
    val apiKey: String
) : Credentials