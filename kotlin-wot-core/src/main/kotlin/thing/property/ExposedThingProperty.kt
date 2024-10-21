package ai.ancf.lmos.wot.thing.property

import ai.ancf.lmos.wot.thing.ExposedThing
import java.util.*
import java.util.concurrent.CompletableFuture

class ExposedThingProperty<T>(
    name: String,
    thing: ExposedThing,
    state: PropertyState<T>,
    objectType: String,
    description: String? = null,
    descriptions: Map<String, String>? = null,
    type: String = "string",
    observable: Boolean = false,
    readOnly: Boolean = false,
    writeOnly: Boolean = false,
    uriVariables: Map<String, Map<String, Any?>?>? = null,
    optionalProperties: Map<String, Any> = emptyMap()
) : ThingProperty<T>(
    objectType,
    type,
    observable,
    readOnly,
    writeOnly,
    optionalProperties
) {
    private val name: String = name
    private val thing: ExposedThing = thing

    @com.fasterxml.jackson.annotation.JsonIgnore
    private val state: PropertyState<T> = state

    // Constructor accepting a ThingProperty
    constructor(name: String, property: ThingProperty<T>?, thing: ExposedThing) : this(
        name,
        thing,
        PropertyState(),
        property?.objectType ?: "string",
        property?.description,
        property?.descriptions,
        property?.type ?: "string",
        property?.isObservable ?: false,
        property?.isReadOnly ?: false,
        property?.isWriteOnly ?: false,
        property?.uriVariables,
        property?.optionalProperties ?: emptyMap()
    )

    suspend fun read(): CompletableFuture<T> {
        return if (state.readHandler != null) {
            log.debug("'{}' calls registered readHandler for Property '{}'", thing.id, name)

            try {
                state.readHandler!!.get().whenComplete { customValue, e -> state.setValue(customValue) }
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

    suspend fun write(value: T): CompletableFuture<T?> {
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
                        state.setValue(customValue)
                        state.emit(customValue)
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
    
    fun getState(): PropertyState<T> {
        return state
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
                ", type='${type}'"
                ", readOnly=$isReadOnly" +
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
