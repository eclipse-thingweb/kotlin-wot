/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.binding.websocket

import ai.ancf.lmos.wot.security.SecurityScheme

data class HttpClientConfig(
    val port: Int?,
    val address: String?,
    val allowSelfSigned: Boolean,
    val serverKey: String,
    val serverCert: String,
    val security: SecurityScheme
)
