package ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.schema.ContentListener
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.InteractionAffordance
import ai.ancf.lmos.wot.thing.schema.InteractionInput
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ProtocolListenerRegistry {

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
        val formMap = listeners[affordance] ?: emptyMap()

        if (formIndex != null) {
            formMap[formIndex]?.let { listenersForIndex ->
                val contentType = affordance.forms[formIndex].contentType
                val content = ContentManager.valueToContent(data, contentType)
                listenersForIndex.forEach { it.handle(content) }
                return
            }
        }

        formMap.forEach { (index, listenersForIndex) ->
            val contentType = affordance.forms[index].contentType
            val content = ContentManager.valueToContent(data, contentType)
            listenersForIndex.forEach { it.handle(content) }
        }
    }
}