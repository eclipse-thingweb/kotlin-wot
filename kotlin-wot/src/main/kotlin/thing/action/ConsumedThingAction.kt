package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.ServientException
import ai.ancf.lmos.wot.ai.ancf.lmos.wot.thing.ContentCodecException
import ai.ancf.lmos.wot.ai.ancf.lmos.wot.thing.ContentManager
import ai.ancf.lmos.wot.thing.schema.ActionAffordance
import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.anfc.lmos.wot.binding.Content
import ai.anfc.lmos.wot.binding.ProtocolClient
import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory

/**
 * Used in combination with [ConsumedThing] and allows consuming of a [ThingAction].
 */
data class ConsumedThingAction<I, O>(
    private val action: ThingAction<I, O>,
    @JsonIgnore
    private val thing: Thing,
    @Transient private val state: ExposedThingAction.ActionState<I, O> = ExposedThingAction.ActionState()
) : ActionAffordance<I, O> by action {


    /**
     * Invokes this action without parameters.
     *
     *
     * @return
     */
    suspend fun invoke(): O? {
        return invoke(emptyMap())
    }

    /**
     * Invokes this action and passes parameters to it.
     *
     * @param parameters contains a map with the names of the uri variables as keys and
     * corresponding values (ex. `Map.of("step", 3)`).
     * @return
    ` */
    suspend fun invoke(parameters: Map<String, Any> = emptyMap()): O? {
        try {
            val clientAndForm: Pair<ProtocolClient, Form> = thing.getClientFor(forms, Operation.INVOKE_ACTION)
            val client: ProtocolClient = clientAndForm.first
            var form: Form = clientAndForm.second
            log.debug(
                "Thing '{}' invoking Action '{}' with form '{}' and parameters '{}'",
                thing.id,
                title,
                form.href,
                parameters
            )
            var input: Content? = null
            if (parameters.isNotEmpty()) {
                input = ContentManager.valueToContent(parameters, form.contentType)
            }
            form = ConsumedThing.handleUriVariables(form, parameters)
            val result = client.invokeResource(form, input)
            try {
                return ContentManager.contentToValue(result, output)
            } catch (e: ContentCodecException) {
                throw ConsumedThingException("Received invalid writeResource from Thing: " + e.message)
            }
        } catch (e: ContentCodecException) {
            throw ConsumedThingException("Received invalid input: " + e.message)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConsumedThingAction::class.java)
    }
}


open class ConsumedThingException : ServientException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}
