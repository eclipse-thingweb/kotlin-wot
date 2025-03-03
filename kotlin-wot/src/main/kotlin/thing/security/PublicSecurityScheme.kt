/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.security

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Raw public key asymmetric key security configuration identified by the term public (i.e.,
 * "scheme": "public").<br></br> See also: https://www.w3.org/2019/wot/security#publicsecurityscheme
 */
class PublicSecurityScheme(@field:JsonInclude(JsonInclude.Include.NON_EMPTY) val identity: String) : SecurityScheme {

    override fun toString(): String {
        return "PublicSecurityScheme{" +
                "identity='" + identity + '\'' +
                '}'
    }
}
