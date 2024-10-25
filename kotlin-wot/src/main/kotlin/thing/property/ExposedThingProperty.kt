package ai.ancf.lmos.wot.thing.property


import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.ThingInteraction
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.VariableDataSchema
import ai.ancf.lmos.wot.thing.schema.VariableSchema
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.CompletableFuture

class ExposedThingProperty<T>(
    val name: String,
    val thing : Thing,
    val state: PropertyState<T>,
    objectType: String? = null,
    description: String? = null,
    descriptions: Map<String, String>? = null,
    type: String = "string",
    observable: Boolean = false,
    readOnly: Boolean = false,
    writeOnly: Boolean = false,
    uriVariables: Map<String, Map<String, VariableSchema>>? = null,
    optionalProperties: Map<String, VariableSchema> = emptyMap()
) : ThingProperty<T>(
    objectType,
    type,
    observable,
    readOnly,
    writeOnly,
    optionalProperties,
    description,
    descriptions,
    null,
    uriVariables,
) {

    // Constructor accepting a ThingProperty
    constructor(name: String, property: ThingProperty<T>?, thing: Thing) : this(
        name,
        thing,
        PropertyState(),
        property?.objectType ?: "string",
        property?.description,
        property?.descriptions,
        property?.type ?: "string",
        property?.observable ?: false,
        property?.readOnly ?: false,
        property?.writeOnly ?: false,
        property?.uriVariables,
        property?.optionalProperties ?: emptyMap()
    )

    suspend fun read(): CompletableFuture<T> {
        return if (state.readHandler != null) {
            log.debug("'{}' calls registered readHandler for Property '{}'", thing.id, name)

            try {
                state.readHandler!!.get().whenComplete { customValue, e ->
                    runBlocking {
                        state.setValue(customValue)
                    }
                }
            } catch (e: Exception) {
                CompletableFuture.failedFuture<T>(e)
            }
        } else {
            val future = CompletableFuture<T>()
            val value: T? = state.value
            log.debug("'{}' gets internal value '{}' for Property '{}'", thing.id, value, name)
            future.complete(value)
            future
        }
    }

    suspend fun write(value: T): CompletableFuture<T> {
        return if (state.writeHandler != null) {
            log.debug("'{}' calls registered writeHandler for Property '{}'", thing.id, name)
            try {
                state.writeHandler!!.apply(value).whenComplete { customValue, e ->
                    log.debug(
                        "'{}' write handler for Property '{}' sets custom value '{}'",
                        thing.id,
                        name,
                        customValue
                    )
                    if (state.value != customValue) {
                        runBlocking {
                            state.setValue(customValue)
                            state.emit(customValue)
                        }
                    }
                }
            } catch (e: Exception) {
                CompletableFuture.failedFuture<T?>(e)
            }
        } else {
            if (state.value != value) {
                log.debug("'{}' sets Property '{}' to internal value '{}'", thing.id, name, value)
                state.setValue(value)
                state.emit(value)
            }
            CompletableFuture.completedFuture<T?>(null)
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), name, thing, state)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        if (!super.equals(o)) return false
        val that = o as ExposedThingProperty<*>
        return name == that.name && thing == that.thing && state == that.state
    }

    override fun toString(): String {
        return "ExposedThingProperty{" +
                "name='$name'" +
                ", state=$state" +
                ", objectType='${objectType}'" +
                ", type='${type}'" +
                ", readOnly=$readOnly" +
                ", writeOnly=$writeOnly" +
                ", optionalProperties=$optionalProperties" +
                ", description='$description'" +
                ", descriptions=$descriptions" +
                ", uriVariables=$uriVariables" +
                '}'
    }

    companion object {
        private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(ExposedThingProperty::class.java)
    }
}

sealed class ThingProperty<T>(

    @JsonProperty("@type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val objectType: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    override val type: String = "string",

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val observable: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val readOnly: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val writeOnly: Boolean = false,

    @JsonAnyGetter
    val optionalProperties: Map<String, VariableSchema> = emptyMap(),

    override val description: String? = null,
    override val descriptions: Map<String, String>? = null,
    override val forms: MutableList<Form>?,
    override val uriVariables: Map<String, Map<String, VariableSchema>>? = emptyMap(),

    ) : ThingInteraction<ThingProperty<T>?>(), DataSchema {

    override val classType: Class<*>
        get() = VariableDataSchema(type).classType

    fun getOptional(name: String): Any? {
        return optionalProperties[name]
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), objectType, type, observable, readOnly, writeOnly, optionalProperties)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is ThingProperty<*>) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        val that: ThingProperty<*> = o
        return observable == that.observable && readOnly == that.readOnly && writeOnly == that.writeOnly && objectType == that.objectType && type == that.type && optionalProperties == that.optionalProperties
    }

    override fun toString(): String {
        return "ThingProperty{" +
                "objectType='" + objectType + '\'' +
                ", type='" + type + '\'' +
                ", observable=" + observable +
                ", readOnly=" + readOnly +
                ", writeOnly=" + writeOnly +
                ", optionalProperties=" + optionalProperties +
                ", description='" + description + '\'' +
                ", descriptions=" + descriptions +
                ", forms=" + forms +
                ", uriVariables=" + uriVariables +
                '}'
    }
}

