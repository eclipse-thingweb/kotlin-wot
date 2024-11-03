package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.Servient
import ai.ancf.lmos.wot.ai.ancf.lmos.wot.thing.ContentCodecException
import ai.ancf.lmos.wot.ai.ancf.lmos.wot.thing.ContentManager
import ai.ancf.lmos.wot.ai.ancf.lmos.wot.thing.UriTemplate
import ai.ancf.lmos.wot.thing.action.ConsumedThingException
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.ObjectSchema
import ai.ancf.lmos.wot.thing.schema.ThingDescription
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletionException

/**
 * Represents an object that extends a Thing with methods for client interactions (send request for
 * reading and writing Properties), invoke Actions, subscribe and unsubscribe for Property changes
 * and Events. https://w3c.github.io/wot-scripting-api/#the-consumedthing-interface
 */
class ConsumedThing(private val servient: Servient, private val thing: Thing) : ThingDescription by thing {

    private val clients: MutableMap<String, ProtocolClient> = mutableMapOf()
    fun getClientFor(
        form: Form,
        op: Operation
    ): Pair<ProtocolClient?, Form> {
        return getClientFor(listOf(form), op)
    }

    /**
     * Searches and returns a ProtocolClient in given `forms` that matches the given
     * `op`. Throws an exception when no client can be found.
     *
     * @param forms
     * @param op
     * @return
     * @throws ConsumedThingException
     */

    fun getClientFor(forms: List<Form>, op: Operation): Pair<ProtocolClient, Form> {
        require(forms.isNotEmpty()) { "No forms available for operation $op on $id" }

        // Get supported schemes in the order of preference
        val supportedSchemes = servient.getClientSchemes()

        // Find schemes in forms sorted by supported schemes preference
        val schemes = forms.asSequence()
            .mapNotNull { it.hrefScheme }
            .distinct()
            .sortedBy { supportedSchemes.indexOf(it).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
            .toSet()

        require(schemes.isNotEmpty()) { "No schemes in forms found" }

        // Try finding a cached client for one of the schemes
        val (scheme, client) = schemes
            .firstNotNullOfOrNull { scheme -> clients[scheme]?.let { scheme to it } }
            ?: initNewClientFor(schemes).also { (newScheme, newClient) ->
                clients[newScheme] = newClient
                log.debug("'$id' got new client for scheme '$newScheme'")
            }

        log.debug("'$id' chose client for scheme '$scheme'")

        val form = getFormForOpAndScheme(forms, op, scheme)
        return client to form
    }

    private fun initNewClientFor(schemes: Set<String>): Pair<String, ProtocolClient> {
        try {
            schemes.forEach { scheme ->

                if (servient.hasClientFor(scheme)) {
                    val client = servient.getClientFor(scheme)

                    // Initialize client security system if security details are provided
                    security.takeIf { it.isNotEmpty() }?.let {
                        log.debug("'{}' setting credentials for '{}'", id, client)

                        val metadata = security.mapNotNull { key -> securityDefinitions[key] }
                        client.setSecurity(metadata, servient.getCredentials(id))
                    }
                    return scheme to client
                }
            }
            throw NoClientFactoryForSchemesConsumedThingException(id, schemes)
        } catch (e: ProtocolClientException) {
            throw ConsumedThingException("Unable to create client: ${e.message}")
        }
    }

    private fun getFormForOpAndScheme(
        forms: List<Form>,
        op: Operation,
        scheme: String?
    ): Form {
        // Find a form that matches the operation and scheme
        return forms.firstOrNull { it.op?.contains(op) == true && it.hrefScheme == scheme }
            ?: forms.firstOrNull { it.op.isNullOrEmpty() && it.hrefScheme == scheme }
            ?: throw NoFormForInteractionConsumedThingException(id, op)
    }

    suspend fun readProperties(vararg names: String): Map<Any, Any> {
        return readProperties(listOf(*names))
    }

    /**
     * Returns the values of the properties contained in `names`.
     *
     * @param names
     * @return
     */
    suspend fun readProperties(names: List<String>): Map<Any, Any> {
        val values = readProperties()

        return values.filter { (key, _) -> key in names }
            .mapKeys { it.key }
    }

    /**
     * Returns the values of all properties.
     *
     * @return
     */
    suspend fun readProperties(): Map<Any, Any> {
        val clientAndForm: Pair<ProtocolClient, Form> = getClientFor(forms, Operation.READ_ALL_PROPERTIES)
        val client = clientAndForm.first
        val form: Form = clientAndForm.second
        log.debug("'{}' reading '{}'", id, form.href)
        val result = client.readResource(form)
        try {
            return ContentManager.contentToValue(result, ObjectSchema())
        } catch (e: ContentCodecException) {
            throw CompletionException(ConsumedThingException("Received invalid writeResource from Thing: " + e.message))
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConsumedThing::class.java)

        /**
         * Creates new form (if needed) for URI Variables http://192.168.178.24:8080/counter/actions/increment{?step}
         * with '{'step' : 3}' -&gt; http://192.168.178.24:8080/counter/actions/increment?step=3.<br></br>
         * see RFC6570 (https://tools.ietf.org/html/rfc6570) for URI Template syntax
         */
        fun handleUriVariables(form: Form, parameters: Map<String, Any>): Form {
            val href: String = form.href
            val uriTemplate: UriTemplate = UriTemplate.fromTemplate(href)
            val updatedHref: String = uriTemplate.expand(parameters)
            if (href != updatedHref) {
                // "clone" form to avoid modifying original form
                val updatedForm = Form(updatedHref)
                log.debug("'{}' update URI to '{}'", href, updatedHref)
                return updatedForm
            }
            return form
        }
    }
}


class NoFormForInteractionConsumedThingException : ConsumedThingException {
    constructor(title: String, op: Operation) : super("'$title' has no form for interaction '$op'")
    constructor(message: String?) : super(message)
}


class NoClientFactoryForSchemesConsumedThingException(title: String, schemes: Set<String?>) :
    ConsumedThingException("'$title': Missing ClientFactory for schemes '$schemes'")


