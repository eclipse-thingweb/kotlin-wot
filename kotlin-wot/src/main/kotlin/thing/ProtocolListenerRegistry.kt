package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.InteractionAffordance
import ai.ancf.lmos.wot.thing.schema.InteractionInput
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ProtocolListenerRegistry {

    private val log: Logger = LoggerFactory.getLogger(ProtocolListenerRegistry::class.java)

    internal val listeners: ConcurrentMap<InteractionAffordance, MutableMap<Int, MutableList<ContentListener>>> = ConcurrentHashMap()

    fun register(affordance: InteractionAffordance, formIndex: Int, listener: ContentListener) {
        val form = affordance.forms.getOrNull(formIndex)
            ?: throw IllegalArgumentException("Can't register listener; no form at index $formIndex for the affordance")

        val formMap = listeners.getOrPut(affordance) { mutableMapOf() }
        val listenersForIndex = formMap.getOrPut(formIndex) { mutableListOf() }

        listenersForIndex.add(listener)
    }

    fun unregister(affordance: InteractionAffordance, formIndex: Int, listener: ContentListener) {
        val formMap = listeners[affordance] ?: throw IllegalStateException("Listener not found for affordance")

        val listenersForIndex = formMap[formIndex] ?: throw IllegalStateException("Form not found at index $formIndex")

        val wasRemoved = listenersForIndex.remove(listener)
        if (!wasRemoved) throw IllegalStateException("Listener not found in the specified form index")
    }

    fun unregisterAll() {
        listeners.clear()
    }


    suspend fun <T> notify(
        affordance: InteractionAffordance,
        data: InteractionInput,
        schema: DataSchema<T>? = null,
        formIndex: Int? = null
    ) {
        log.debug("Notify listeners for affordance with title '${affordance.title}'")
        val formMap = listeners[affordance] ?: emptyMap()

        val interactionInputValue = data as InteractionInput.Value

        if (formIndex != null) {
            formMap[formIndex]?.let { listenersForIndex ->
                val contentType = affordance.forms[formIndex].contentType
                try{
                    val content = ContentManager.valueToContent(interactionInputValue.value, contentType)
                    listenersForIndex.forEach { it.handle(content) }
                    return
                }
                catch (e: Exception){
                    log.error("Error while notifying listeners", e)
                }
            }
        }

        formMap.forEach { (index, listenersForIndex) ->
            log.debug("Notify {} listeners for form {}", listenersForIndex.size, affordance.forms[index])
            val contentType = affordance.forms[index].contentType
            try{
                val content = ContentManager.valueToContent(interactionInputValue.value, contentType)
                listenersForIndex.forEach {
                    it.handle(content)
                }
            }
            catch (e: Exception){
                log.error("Error while notifying listeners", e)
            }
        }
    }
}