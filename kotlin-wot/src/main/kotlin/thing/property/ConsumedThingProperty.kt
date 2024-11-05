package ai.ancf.lmos.wot.thing.property

import ai.ancf.lmos.wot.content.ContentCodecException
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ConsumedThing
import ai.ancf.lmos.wot.thing.action.ConsumedThingException
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.PropertyAffordance
import ai.ancf.lmos.wot.thing.schema.ThingProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory

/**
 * Used in combination with [ConsumedThing] and allows consuming of a [ThingProperty].
 */
data class ConsumedThingProperty<T>(
   private val property: ThingProperty<T>,
   @JsonIgnore
   private val thing: ConsumedThing,
   private val state: ExposedThingProperty.PropertyState<T> = ExposedThingProperty.PropertyState()
) : PropertyAffordance<T> by property {

    private fun normalizeHrefs(forms: List<Form>, thing: ConsumedThing): List<Form> {
        return forms.map { form -> normalizeHref(form, thing) }
    }

    private fun normalizeHref(form: Form, thing: ConsumedThing): Form {
        val base: String? = thing.base
        return if (!base.isNullOrEmpty() && !form.href.matches("^(?i:[a-z+]+:).*".toRegex())) {
            val normalizedHref = base + form.href
            form.copy(href = normalizedHref)
        } else {
            form
        }
    }

    // Suspend function for reading
    suspend fun read(): T? {
        try {
            val (client, form) = thing.getClientFor(forms, Operation.READ_PROPERTY)
            log.debug("Thing '{}' reading Property '{}' from '{}'", thing.id, title, form.href)

            val content = client.readResource(form)
            try {
                return ContentManager.contentToValue(content, this)
            } catch (e: ContentCodecException) {
                throw ConsumedThingException("Received invalid readResource from Thing: ${e.message}", e)
            }
        } catch (e: ConsumedThingException) {
            throw e
        }
    }

    // Suspend function for writing
    suspend fun write(value: T): T? {
        try {
            val (client, form) = thing.getClientFor(forms, Operation.WRITE_PROPERTY)
            log.debug("ConsumedThing {} writing {}", thing.id, form.href)

            val input = ContentManager.valueToContent(value, form.contentType)
            val content = client.writeResource(form, input)
            try {
                return ContentManager.contentToValue(content, this)
            } catch (e: ContentCodecException) {
                throw ConsumedThingException("Received invalid writeResource from Thing: ${e.message}", e)
            }
        } catch (e: ContentCodecException) {
            throw ConsumedThingException("Received invalid input: ${e.message}", e)
        } catch (e: ConsumedThingException) {
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConsumedThingProperty::class.java)
    }
}
