/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Describes properties of a security mechanism (e.g. password authentication).<br></br> See also:
 * https://www.w3.org/TR/wot-thing-description/#security-serialization-json
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "scheme")
@JsonSubTypes(
    JsonSubTypes.Type(value = APIKeySecurityScheme::class, name = "apikey"),
    JsonSubTypes.Type(value = BasicSecurityScheme::class, name = "basic"),
    JsonSubTypes.Type(value = BearerSecurityScheme::class, name = "bearer"),
    JsonSubTypes.Type(value = CertSecurityScheme::class, name = "cert"),
    JsonSubTypes.Type(value = DigestSecurityScheme::class, name = "digest"),
    JsonSubTypes.Type(value = NoSecurityScheme::class, name = "nosec"),
    JsonSubTypes.Type(value = OAuth2SecurityScheme::class, name = "oauth2"),
    JsonSubTypes.Type(value = PoPSecurityScheme::class, name = "pop"),
    JsonSubTypes.Type(value = PSKSecurityScheme::class, name = "psk"),
    JsonSubTypes.Type(value = PublicSecurityScheme::class, name = "public")
)
@JsonIgnoreProperties(ignoreUnknown = true)
interface SecurityScheme
