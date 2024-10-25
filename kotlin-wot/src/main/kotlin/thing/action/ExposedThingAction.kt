package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.ThingInteraction
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.VariableSchema
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import java.util.concurrent.CompletableFuture

//@Serializable
class ExposedThingAction<I, O>(
    val name: String,
    val thing: Thing,
    private val state: ActionState<I, O>,
    description: String?,
    descriptions: Map<String, String>?,
    uriVariables: Map<String, Map<String, VariableSchema>>?,
    forms: MutableList<Form>?,
    objectType: String?,
    input: VariableSchema?,
    output: VariableSchema?
) : ThingAction<I, O>(description, descriptions, uriVariables, forms, objectType, input, output) {

    constructor(name: String, action: ThingAction<I, O>, thing: Thing) : this(
        name,
        thing,
        ActionState(),
        action.description,
        action.descriptions,
        action.uriVariables,
        null,
        action.objectType,
        action.input,
        action.output
    )
    /**
     * Invokes the method and executes the handler defined in [.state]. `input`
     * contains the request payload. `options` can contain additional data (for example,
     * the query parameters when using COAP/HTTP).
     *
     * @param input
     * @param options
     * @return
     */
    /**
     * Invokes the method and executes the handler defined in [.state]. `input`
     * contains the request payload.
     *
     * @param input
     * @return
     */
    /**
     * Invokes the method and executes the handler defined in [.state].
     *
     * @return
     */
    @JvmOverloads
    operator fun invoke(
        input: I,
        options: Map<String, Map<String, VariableSchema>> = emptyMap()
    ): CompletableFuture<O>? {
        log.debug("'{}' has Action state of '{}': {}", thing.id, name, state)
        return if (state.handler != null) {
            log.debug(
                "'{}' calls registered handler for Action '{}' with input '{}' and options '{}'", thing.id,
                name, input, options
            )
            try {
                var output: CompletableFuture<O>? = state.handler?.apply(input, options)
                if (output == null) {
                    log.warn(
                        "'{}': Called registered handler for Action '{}' returned null. This can cause problems. Give Future with null result back.",
                        thing.id,
                        name
                    )
                    output = CompletableFuture.completedFuture(null)
                }
                output
            } catch (e: Exception) {
                CompletableFuture.failedFuture(e)
            }
        } else {
            log.debug("'{}' has no handler for Action '{}'", thing.id, name)
            CompletableFuture.completedFuture(null)
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), name, thing, state)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        val that = o as ExposedThingAction<*, *>
        return name == that.name && thing == that.thing && state == that.state
    }

    override fun toString(): String {
        return ((("ExposedThingAction{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", input=" + input +
                ", output=" + output +
                ", description='" + description + '\'').toString() +
                ", descriptions=" + descriptions).toString() +
                ", forms=" + forms).toString() +
                ", uriVariables=" + uriVariables +
                '}'
    }

    companion object {
        private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(ExposedThingAction::class.java)
    }
}

sealed class ThingAction<I, O>(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    override val description: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    override val descriptions: Map<String, String>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    override val uriVariables: Map<String, Map<String, VariableSchema>>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    override val forms: MutableList<Form>?,

    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val objectType: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val input: VariableSchema? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val output: VariableSchema? = null

) : ThingInteraction<ThingAction<I, O>?>() {

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), objectType, input, output)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is ThingAction<*, *>) return false
        if (!super.equals(o)) return false
        return objectType == o.objectType && input == o.input && output == o.output
    }

    override fun toString(): String {
        return "ThingAction{" +
                "objectType='$objectType', " +
                "input=$input, " +
                "output=$output, " +
                "description='$description', " +
                "descriptions=$descriptions, " +
                "forms=$forms, " +
                "uriVariables=$uriVariables" +
                '}'
    }
}
