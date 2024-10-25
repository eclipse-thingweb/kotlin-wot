package ai.ancf.lmos.wot.thing.form

import ai.ancf.lmos.wot.thing.schema.VariableSchema
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException

data class Form(
    val href: String,

    @JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val op: List<Operation> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val subprotocol: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val contentType: String? = null,

    @get:JsonAnyGetter
    val optionalProperties: Map<String, VariableSchema> = emptyMap()
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

    companion object {
        private val log = LoggerFactory.getLogger(Form::class.java)
    }
}

