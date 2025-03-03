/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.security

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Bearer token authentication security configuration identified by the term bearer (i.e., "scheme":
 * "bearer"). This scheme is intended for situations where bearer tokens are used independently of
 * OAuth2. If the oauth2 scheme is specified it is not generally necessary to specify this scheme as
 * well as it is implied. For format, the value jwt indicates conformance with RFC7519, jws
 * indicates conformance with RFC7797, cwt indicates conformance with RFC8392, and jwe indicates
 * conformance with !RFC7516, with values for alg interpreted consistently with those standards.
 * Other formats and algorithms for bearer tokens MAY be specified in vocabulary extensions.<br></br> See
 * also: https://www.w3.org/2019/wot/security#bearersecurityscheme
 */
class BearerSecurityScheme @JvmOverloads constructor(
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val `in`: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val alg: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val format: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val name: String? = null,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val authorization: String? = null
) : SecurityScheme {

    override fun toString(): String {
        return "BearerSecurityScheme{" +
                "in='" + `in` + '\'' +
                ", alg='" + alg + '\'' +
                ", format='" + format + '\'' +
                ", name='" + name + '\'' +
                ", authorization='" + authorization + '\'' +
                '}'
    }
}
