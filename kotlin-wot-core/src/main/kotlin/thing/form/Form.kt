package ai.ancf.lmos.wot.thing.form

import com.fasterxml.jackson.annotation.*
import com.sun.org.slf4j.internal.LoggerFactory
import java.net.URI
import java.net.URISyntaxException

data class Form(
    val href: String? = null,

    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val op: List<Operation>? = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val subprotocol: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val contentType: String? = null,

    @get:JsonAnyGetter
    val optionalProperties: Map<String, Any> = emptyMap()
) {

    @get:JsonIgnore
    val hrefScheme: String?
        get() = try {
            // remove uri variables first
            val sanitizedHref = href?.substringBefore("{")
            URI(sanitizedHref).scheme
        } catch (e: URISyntaxException) {
            log.warn("Form href is invalid: ", e)
            null
        }

    @JsonAnySetter
    fun setOptionalForJackson(name: String, value: String) {
        (optionalProperties as MutableMap)[name] = value
    }

    fun getOptional(name: String): Any? {
        return optionalProperties[name]
    }

    companion object {
        private val log = LoggerFactory.getLogger(Form::class.java)
    }
}

