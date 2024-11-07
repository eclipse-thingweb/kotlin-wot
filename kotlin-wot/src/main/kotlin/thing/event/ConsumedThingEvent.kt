package ai.ancf.lmos.wot.thing.action

import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ConsumedThingImpl
import ai.ancf.lmos.wot.thing.event.ThingEvent
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.form.Operation
import ai.ancf.lmos.wot.thing.schema.EventAffordance
import ai.anfc.lmos.wot.binding.ProtocolClient
import ai.anfc.lmos.wot.binding.ProtocolClientException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Used in combination with [ConsumedThingImpl] and allows consuming of a [ThingEvent].
 */
data class ConsumedThingEvent<T, S, C>(private val event: ThingEvent<T, S, C>, private val thing: ConsumedThingImpl) : EventAffordance<T, S, C> by event {
    suspend fun observe(): Flow<T> {
        return try {
            val clientAndForm: Pair<ProtocolClient, Form> = thing.getClientFor(forms, Operation.SUBSCRIBE_EVENT)
            val client: ProtocolClient = clientAndForm.first
            val form: Form = clientAndForm.second
            client.observeResource(form).map { ContentManager.contentToValue(it, data!!) }
        } catch (e: ProtocolClientException) {
            throw ConsumedThingException(e)
        }
    }
}
