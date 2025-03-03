/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.filter

import org.eclipse.thingweb.ServientException
import org.eclipse.thingweb.thing.ExposedThing
import org.eclipse.thingweb.thing.schema.WoTExposedThing

/**
 * Is used in the discovery process and filters the things according to certain properties
 */
interface ThingQuery {

    /**
     * Applies the filter to the found things and returns only those things that meet the desired
     * criteria
     *
     * @param things
     * @return
     */
    @Throws(ThingQueryException::class)
    fun filter(things: Collection<WoTExposedThing>): List<ExposedThing>
}

/**
 * This exception is thrown when an invalid query is attempted to be used.
 */
class ThingQueryException : ServientException {
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
}


