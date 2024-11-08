package ai.ancf.lmos.wot.thing.action

/*
/**
 * Used in combination with [ConsumedThingImpl] and allows consuming of a [ThingAction].
 */
data class ConsumedThingAction<I, O>(
    private val action: ThingAction<I, O>,
    @JsonIgnore
    private val thing: ConsumedThing,
    @Transient private val state: ExposedThingAction.ActionState<I, O> = ExposedThingAction.ActionState()
) : ActionAffordance<I, O> by action {

    /**
     * Invokes the method and executes the handler defined in [ExposedThingAction.ActionState]. `input`
     * contains the request payload. `options` can contain additional data (for example,
     * the query parameters when using COAP/HTTP).
     *
     * @param input
     * @param options
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
            form = ConsumedThingImpl.handleUriVariables(form, parameters)
            val result = client.invokeResource(form, input)
            return try {
                if (output != null){
                    ContentManager.contentToValue(result, output!!)
                }else{
                    null
                }
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
    constructor(message: String) : super(message)
    constructor(cause: Throwable?) : super(cause)

    constructor(message: String, cause: Throwable?) : super(cause)
}
*/