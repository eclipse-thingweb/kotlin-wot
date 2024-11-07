package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.form.Form
import kotlinx.coroutines.flow.Flow

class InteractionOutputImpl<T>(
    private val content: Content,
    override val form: Form,
    override val schema: DataSchema<T>
) : InteractionOutput<T> {
    override val data: Flow<ByteArray>?
        get() = TODO("Not yet implemented")

    override var dataUsed: Boolean = false
    private val dataLock = Any() // Synchronization lock for dataUsed

    private val lazyValue: T? by lazy {
        ContentManager.contentToValue(content, schema)
    }
    override suspend fun arrayBuffer(): ByteArray {
        return content.body
    }

    override suspend fun value(): T? {
        return lazyValue
    }
}