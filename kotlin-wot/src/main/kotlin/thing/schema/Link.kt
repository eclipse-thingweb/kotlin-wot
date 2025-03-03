/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.schema

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Represents a link or submission target of a form with metadata that describes the link.
 *
 * @property href The target IRI of the link or submission target of a form. This is a mandatory field.
 * @property type An optional hint indicating the media type RFC2046 of the result of dereferencing the link.
 * @property rel An optional link relation type that identifies the semantics of a link.
 * @property anchor An optional override for the link context, with the given URI or IRI. By default, the context is the Thing itself identified by its ID.
 * @property sizes An optional target attribute specifying one or more sizes for the referenced icon. Only applicable if the relation type is "icon".
 *                The format is {Height}x{Width} (e.g., "16x16", "16x16 32x32").
 * @property hreflang An optional attribute specifying the language of a linked document. The value should be a valid language tag as per [BCP47].
 *                    Can be a single string or an array of strings.
 */
data class Link(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val href: String, // anyURI, mandatory
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val type: String? = null, // Optional media type hint
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val rel: String? = null, // Optional link relation type
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val anchor: String? = null, // Optional override for link context with URI or IRI
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val sizes: String? = null, // Optional sizes for the referenced icon, applicable if rel = "icon"
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val hreflang: List<String>? = null // Optional list of valid language tags
)