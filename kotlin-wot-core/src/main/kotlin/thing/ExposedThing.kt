package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.action.ExposedThingAction
import ai.ancf.lmos.wot.thing.event.ExposedThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.property.ExposedThingProperty
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

class ExposedThing(
    objectType: Type?,
    objectContext: Context?,
    id: String,
    title: String?,
    titles: Map<String, String>?,
    description: String?,
    descriptions: Map<String, String>?,
    forms: List<Form>,
    security: List<String>,
    securityDefinitions: Map<String, SecurityScheme>,
    base: String?,
    metadata: Map<String, Any>,
    properties: Map<String, ExposedThingProperty<Any>>,
    actions: Map<String, ExposedThingAction<Any, Any>>,
    events: Map<String, ExposedThingEvent<Any>>
) : Thing<ExposedThingProperty<Any>, ExposedThingAction<Any, Any>, ExposedThingEvent<Any>>(
    objectType,
    objectContext,
    id,
    title,
    titles,
    description,
    descriptions,
    properties,
    actions,
    events,
    forms,
    security,
    securityDefinitions,
    base,
    metadata
) {

    /*
    // Constructor accepting a Thing instance
    constructor(thing: Thing<ExposedThingProperty<Any?>, ExposedThingAction<Any?, Any?>, ExposedThingEvent<Any?>>) :
            this(
                thing.objectType,
                thing.objectContext,
                thing.id,
                thing.title,
                thing.titles,
                thing.description,
                thing.descriptions,
                thing.forms,
                thing.security,
                thing.securityDefinitions,
                thing.base,
                thing.metadata,
                // Create properties, actions, and events from the given Thing instance
                thing.properties.mapValues { (name, property) -> ExposedThingProperty(name, property, this) },
                thing.actions.mapValues { (name, action) -> ExposedThingAction(name, action, this) },
                thing.events.mapValues { (name, event) -> ExposedThingEvent(name, event) }
            )
     */

    /**
     * Returns a [Map] with property names as map key and property values as map value.
     *
     * @return
     */
    suspend fun readProperties(): CompletableFuture<Map<String, Any>> {
        val futures: MutableList<CompletableFuture<*>> = ArrayList()
        val values: MutableMap<String, Any> = HashMap()
        properties.forEach { (name, property) ->
            val readFuture: CompletableFuture<Any> = property.read()
            val putValuesFuture = readFuture.thenApply { value: Any? -> values[name] = value as Any }
            futures.add(putValuesFuture)
        }

        // wait until all properties have been read
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { values }
    }

    /**
     * Writes the transferred `values` to the respective properties and returns the new
     * value of the respective properties.
     *
     * @param values
     * @return
     */
    suspend fun writeProperties(values: Map<String, Any>): CompletableFuture<Map<String, Any>> {
        val futures: MutableList<CompletableFuture<*>> = ArrayList()
        val returnValues: MutableMap<String, Any> = HashMap()
        values.forEach { (name, value) ->
            val property: ExposedThingProperty<Any>? = properties[name]
            if (property != null) {
                val future: CompletableFuture<Any> = property.write(value)
                futures.add(future)
                future.whenComplete { _, _ -> returnValues[name] = value }
            }
        }

        // wait until all properties have been written
        return CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { returnValues }
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode())
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        if (!super.equals(o)) return false
        val that = o as ExposedThing
        return id == that.id
    }

    override fun toString(): String {
        return "ExposedThing{" +
                "objectType='$objectType'" +
                ", objectContext=$objectContext" +
                ", id='$id'" +
                ", title='$title'" +
                ", titles=$titles" +
                ", description='$description'" +
                ", descriptions=$descriptions" +
                ", properties=$properties" +
                ", actions=$actions" +
                ", events=$events" +
                ", forms=$forms" +
                ", security=$security" +
                ", securityDefinitions=$securityDefinitions" +
                ", base='$base'" +
                '}'
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExposedThing::class.java)
    }
}