/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.kotlinsdk.server

import ai.ancf.lmos.sdk.model.AgentRequest
import ai.ancf.lmos.sdk.model.AgentResult


data class LmosAgent(
    var id: String,
    var title: String,
    var description: String,
    var capabilities: String,
    var chat: (input: AgentRequest) -> AgentResult,
    // var events: Flow<String>
)