/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.anfc.lmos.wot.binding

import ai.ancf.lmos.wot.ServientException


/**
 * A ProtocolServerException is thrown by [ProtocolServer] implementations when errors occur.
 */
open class ProtocolServerException : ServientException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor() : super()
}
