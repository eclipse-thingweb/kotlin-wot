/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.thing.filter

import java.net.URI

/**
 * ThingFilter is used for the discovery process and specifies what things to look for and where to
 * look for them.
 */
data class ThingFilter(val url: URI? = null, val query: ThingQuery? = null, var method: DiscoveryMethod = DiscoveryMethod.ANY) {

    override fun toString(): String {
        return "ThingFilter{" +
                "method=" + method +
                ", url=" + url +
                ", query=" + query +
                '}'
    }
}
