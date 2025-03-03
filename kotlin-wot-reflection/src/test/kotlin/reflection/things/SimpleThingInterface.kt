/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.reflection.things

import org.eclipse.thingweb.reflection.annotations.Action

interface SimpleThingInterface {

    @Action()
    suspend fun outputAction() : String
}