/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.security

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Digest authentication security configuration identified by the term digest (i.e., "scheme":
 * "digest"). This scheme is similar to basic authentication but with added features to avoid
 * man-in-the-middle attacks.<br></br> See also: https://www.w3.org/2019/wot/security#digestsecurityscheme
 */
class DigestSecurityScheme(
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val `in`: String, @field:JsonInclude(
        JsonInclude.Include.NON_EMPTY
    ) val name: String, @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val qop: String
) : SecurityScheme {

    override fun toString(): String {
        return "DigestSecurityScheme{" +
                "in='" + `in` + '\'' +
                ", name='" + name + '\'' +
                ", qop='" + qop + '\'' +
                '}'
    }
}
