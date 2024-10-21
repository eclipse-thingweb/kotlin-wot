package ai.ancf.lmos.wot.security

import com.fasterxml.jackson.annotation.JsonInclude
import thing.security.SecurityScheme

/**
 * Proof-of-possession (PoP) token authentication security configuration identified by the term pop
 * (i.e., "scheme": "pop"). Here jwt indicates conformance with !RFC7519, jws indicates conformance
 * with !RFC7797, cwt indicates conformance with !RFC8392, and jwe indicates conformance with
 * RFC7516, with values for alg interpreted consistently with those standards. Other formats and
 * algorithms for PoP tokens MAY be specified in vocabulary extensions.<br></br> See also:
 * https://www.w3.org/2019/wot/security#popsecurityscheme
 */
class PoPSecurityScheme(
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val `in`: String,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val name: String,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val format: String,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val authorization: String,
    @field:JsonInclude(JsonInclude.Include.NON_EMPTY) val alg: String
) : SecurityScheme {

    override fun toString(): String {
        return "PoPSecurityScheme{" +
                "in='" + `in` + '\'' +
                ", name='" + name + '\'' +
                ", format='" + format + '\'' +
                ", authorization='" + authorization + '\'' +
                ", alg='" + alg + '\'' +
                '}'
    }
}
