/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.security

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * OAuth2 authentication security configuration for systems conformant with !RFC6749 and !RFC8252,
 * identified by the term oauth2 (i.e., "scheme": "oauth2"). For the implicit flow authorization
 * MUST be included. For the password and client flows token MUST be included. For the code flow
 * both authorization and token MUST be included. If no scopes are defined in the SecurityScheme
 * then they are considered to be empty.<br></br> See also: https://www.w3.org/2019/wot/security#oauth2securityscheme
 */
class OAuth2SecurityScheme(
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val authorization: String?,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val flow: String,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val token: String?,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val refresh: String?,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val scopes: List<String>?
) : SecurityScheme {

    override fun toString(): String {
        return "OAuth2SecurityScheme{" +
                "authorization='" + authorization + '\'' +
                ", flow='" + flow + '\'' +
                ", token='" + token + '\'' +
                ", refresh='" + refresh + '\'' +
                ", scopes=" + scopes +
                '}'
    }
}
