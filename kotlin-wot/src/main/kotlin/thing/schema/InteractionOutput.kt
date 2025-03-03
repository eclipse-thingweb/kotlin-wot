/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing.schema

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.content.ContentManager
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.flow.Flow

class InteractionOutput(
    private val content: Content,
    override val schema: DataSchema<*>?
) : WoTInteractionOutput {
    override val data: Flow<ByteArray>?
        get() = TODO("Not yet implemented")

    override var dataUsed: Boolean = false

    private val lazyValue: JsonNode? by lazy {
        schema?.let { ContentManager.contentToValue(content, schema) }
    }
    override fun arrayBuffer(): ByteArray {
        return content.body
    }

    override fun value(): JsonNode {
        return ContentManager.contentToValue(content, schema)
    }
}