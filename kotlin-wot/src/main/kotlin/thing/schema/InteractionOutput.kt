package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import kotlinx.coroutines.flow.Flow

class InteractionOutput(
    private val content: Content,
    override val schema: DataSchema<*>?
) : WoTInteractionOutput {
    override val data: Flow<ByteArray>?
        get() = TODO("Not yet implemented")

    override var dataUsed: Boolean = false

    private val lazyValue: DataSchemaValue? by lazy {
        schema?.let { ContentManager.contentToValue(content, schema) }
    }
    override suspend fun arrayBuffer(): ByteArray {
        return content.body
    }

    override suspend fun value(): DataSchemaValue? {
        return lazyValue
    }
}