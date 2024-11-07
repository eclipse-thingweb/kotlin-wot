package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.thing.ExposedThingImpl
import ai.ancf.lmos.wot.thing.Type
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.ActionAffordance
import ai.ancf.lmos.wot.thing.schema.ActionHandler
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.*
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Serializable
data class ExposedThingAction<I, O>(
    private val action: ThingAction<I, O> = ThingAction(),
    @JsonIgnore
    private val thing: ExposedThingImpl = ExposedThingImpl(),
    private val state: ActionState<I, O> = ActionState()
) : ActionAffordance<I, O> by action {
    /**
     * Invokes the method and executes the handler defined in [.state]. `input`
     * contains the request payload. `options` can contain additional data (for example,
     * the query parameters when using COAP/HTTP).
     *
     * @param input
     * @param options
     * @return
     */
    suspend fun invokeAction(
        input: Any?,
        options: Map<String, Map<String, Any>>? = mapOf()
    ): Any? {
        log.debug("'{}' has Action state of '{}': {}", thing.id, title, state)
        return if (state.handler != null) {
            log.debug(
                "'{}' calls registered handler for Action '{}' with input '{}' and options '{}'",
                thing.id, title, input, options
            )
            try {
                // Use the handler as a suspending function directly
                state.handler.handle(input as I, options).also { output ->
                    if (output == null) {
                        log.warn(
                            "'{}': Called registered handler for Action '{}' returned null. This can cause problems.",
                            thing.id, title
                        )
                    }
                }
            } catch (e: Exception) {
                log.error("'{}' handler invocation for Action '{}' failed with exception", thing.id, title, e)
                throw e
            }
        } else {
            log.debug("'{}' has no handler for Action '{}'", thing.id, title)
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExposedThingAction<*, *>

        if (action != other.action) return false
        if (thing != other.thing) return false

        return true
    }

    override fun hashCode(): Int {
        var result = action.hashCode()
        result = 31 * result + thing.hashCode()
        return result
    }


    companion object {
        private val log: org.slf4j.Logger = LoggerFactory.getLogger(ExposedThingAction::class.java)
    }

    data class ActionState<I, O>(val handler: ActionHandler<I, O>? = null)
}

@Serializable
data class ThingAction<I, O>(
    @JsonInclude(NON_EMPTY)
    override var title: String? = null,

    @JsonInclude(NON_EMPTY)
    override var description: String? = null,

    @JsonInclude(NON_EMPTY)
    override var descriptions: MutableMap<String, String>? = null,

    @JsonInclude(NON_EMPTY)
    override var uriVariables: MutableMap<String, DataSchema<@Contextual Any>>? = null,

    @JsonInclude(NON_EMPTY)
    override var forms: MutableList<Form> = mutableListOf(),

    @JsonProperty("@type")
    @JsonInclude(NON_EMPTY)
    override var objectType: Type? = null,

    @JsonInclude(NON_NULL)
    override var input: DataSchema<I>? = null,

    @JsonInclude(NON_NULL)
    override var output: DataSchema<O>? = null,

    @JsonInclude(NON_DEFAULT)
    override var safe: Boolean = false,

    @JsonInclude(NON_DEFAULT)
    override var idempotent: Boolean = false,

    @JsonInclude(NON_NULL)
    override var synchronous: Boolean? = null,

    @JsonInclude(NON_EMPTY)
    override var titles: MutableMap<String, String>? = null,

    @JsonIgnore
    var actionHandler: ActionHandler<I, O>? = null
) : ActionAffordance<I, O> {
}

