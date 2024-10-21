package ai.ancf.lmos.wot.security

import com.fasterxml.jackson.annotation.JsonInclude
import thing.security.SecurityScheme

/**
 * Pre-shared key authentication security configuration identified by the term psk (i.e., "scheme":
 * "psk").<br></br> See also: https://www.w3.org/2019/wot/security#psksecurityscheme
 */
class PSKSecurityScheme(@field:JsonInclude(JsonInclude.Include.NON_EMPTY) val identity: String) : SecurityScheme {

    override fun toString(): String {
        return "PSKSecurityScheme{" +
                "identity='" + identity + '\'' +
                '}'
    }
}
