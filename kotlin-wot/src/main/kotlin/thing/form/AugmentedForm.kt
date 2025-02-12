
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.WoTForm
import ai.ancf.lmos.wot.thing.schema.WoTThingDescription
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException

/**
 * A [Form] augmented with information from its associated [thingDescription]
 * and [interactionAffordance].
 */
data class AugmentedForm(
    private val form: WoTForm,
    private val thingDescription: WoTThingDescription
) : WoTForm by form {

    companion object {
        private val log = LoggerFactory.getLogger(AugmentedForm::class.java)
    }



    @get:JsonIgnore
    val hrefScheme: String?
        get() = try {
            // remove uri variables first
            val sanitizedHref = href.substringBefore("{")
            URI(sanitizedHref).scheme
        } catch (e: URISyntaxException) {
            log.warn("Form href is invalid: ", e)
            null
        }

    override val href: String
        get() {
            val href = thingDescription.base?.let {
                // Remove trailing slash from base if it exists and leading slash from href if it exists
                val baseUrl = it.trimEnd('/') // Remove trailing slash from base
                val formHref = form.href.trimStart('/') // Remove leading slash from href
                "$baseUrl/$formHref" // Concatenate with a single slash in between
            } ?: form.href
            val hrefUriVariables = filterUriVariables(href)

            if (hrefUriVariables.isEmpty()) return href

            return href
            /*

            val affordanceUriVariables = mutableMapOf<String, JsonNode>().apply {
                putAll(thingDescription.uriVariables ?: emptyMap())
                putAll(interactionAffordance.uriVariables ?: emptyMap())
            }

            userProvidedUriVariables?.let {
                validateUriVariables(hrefUriVariables, affordanceUriVariables, it)
            }

            val decodedHref = href.toString().decode()
            val expandedHref = UriTemplate(decodedHref).expand(userProvidedUriVariables ?: emptyMap())
            return URI(expandedHref)

             */
        }

    /**
     * The computed list of [SecurityScheme]s associated with this form.
     */
    val securityDefinitions: List<SecurityScheme>
        get() = thingDescription.securityDefinitions.entries
            .filter { form.security?.contains(it.key) ?: false }
            .map { it.value }

    private fun filterUriVariables(href: String): List<String> {
        val regex = Regex("\\{[?+#./;&]?([^}]+)}") // Extracts text inside `{}` while ignoring optional prefix
        return regex.findAll(href)
            .map { it.groupValues[1] } // Directly access group value (instead of using `?.value`)
            .flatMap { it.split(",") } // Handle multiple variables inside `{}` (comma-separated)
            .map { it.trim() } // Ensure clean variable names
            .toList()
    }

    private fun validateUriVariables(
        uriVariablesInHref: List<String>,
        affordanceUriVariables: Map<String, JsonNode>,
        userProvidedUriVariables: Map<String, Any>
    ) {
        val uncoveredHrefUriVariables = uriVariablesInHref.filterNot { affordanceUriVariables.containsKey(it) }

        if (uncoveredHrefUriVariables.isNotEmpty()) {
            throw IllegalArgumentException(
                "The following URI template variables defined in the form's href " +
                        "but are not covered by a uriVariable entry at the TD or affordance " +
                        "level: ${uncoveredHrefUriVariables.joinToString(", ")}.")
        }

        /*
        affordanceUriVariables.forEach { (key, schemaValue) ->
            userProvidedUriVariables[key]?.let { userProvidedValue ->
                val schema = JsonSchema.create(schemaValue.toJson())
                val result = schema.validate(userProvidedValue)
                if (!result.isValid) {
                    throw IllegalArgumentException("Invalid type for URI variable $key")
                }
            }
        }
        */
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AugmentedForm

        return form == other.form
    }

    override fun hashCode(): Int {
        return form.hashCode()
    }
}
