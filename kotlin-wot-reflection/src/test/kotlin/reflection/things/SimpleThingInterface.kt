/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.reflection.things

import ai.ancf.lmos.wot.reflection.annotations.Action

interface SimpleThingInterface {

    @Action()
    suspend fun outputAction() : String
}