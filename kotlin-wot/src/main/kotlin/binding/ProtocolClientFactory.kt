/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.anfc.lmos.wot.binding


/**
 * A ProtocolClientFactory is responsible for creating new [ProtocolClient] instances. There
 * is a separate client instance for each [ConsumedThing].
 */
interface ProtocolClientFactory {

    val scheme: String

    /**
     * Is called on servient start.
     *
     * @return
     */
    suspend fun init()

    /**
     * Is called on servient shutdown.
     *
     * @return
     */
    suspend fun destroy()

    /** Creates a new [ProtocolClient] */
    fun createClient(): ProtocolClient
}
