/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.anfc.lmos.wot.binding

import org.eclipse.thingweb.ServientException


/**
 * A ProtocolClientException is thrown by [ProtocolClient] implementations when errors occur.
 */
open class ProtocolClientException : ServientException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Exception): super(message, cause)
}
