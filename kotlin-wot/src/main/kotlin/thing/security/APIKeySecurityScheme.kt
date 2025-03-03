/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.security

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * API key authentication security configuration identified by the term apikey (i.e., "scheme":
 * "apikey"). This is for the case where the access token is opaque and is not using a standard
 * token format.<br></br> See also: https://www.w3.org/2019/wot/security#apikeysecurityscheme
 */
class APIKeySecurityScheme(
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val `in`: String, @field:JsonInclude(
        JsonInclude.Include.NON_EMPTY
    ) val name: String
) : SecurityScheme {

    override fun toString(): String {
        return "APIKeySecurityScheme{" +
                "in='" + `in` + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}
