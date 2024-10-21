package ai.ancf.lmos.wot.security

import com.fasterxml.jackson.annotation.JsonInclude
import thing.security.SecurityScheme

/**
 * Certificate-based asymmetric key security configuration conformant with X509V3 identified by the
 * term cert (i.e., "scheme": "cert").<br></br> See also: https://www.w3.org/2019/wot/security#certsecurityscheme
 */
class CertSecurityScheme(@field:JsonInclude(JsonInclude.Include.NON_EMPTY) val identity: String) : SecurityScheme {

    override fun toString(): String {
        return "CertSecurityScheme{" +
                "identity='" + identity + '\'' +
                '}'
    }
}
