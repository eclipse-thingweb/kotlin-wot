/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.security


/**
 * A security configuration corresponding to identified by the term nosec (i.e., "scheme": "nosec"),
 * indicating there is no authentication or other mechanism required to access the resource.<br></br> See
 * also: https://www.w3.org/2019/wot/security#nosecurityscheme
 */
class NoSecurityScheme : SecurityScheme {
    override fun hashCode(): Int {
        return 42
    }

    override fun equals(o: Any?): Boolean {
        return if (this === o) {
            true
        } else o != null && javaClass == o.javaClass
    }

    override fun toString(): String {
        return "NoSecurityScheme{}"
    }
}
