/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.thingweb.thing.form

import org.eclipse.thingweb.thing.OperationsDeserializer
import org.eclipse.thingweb.thing.schema.WoTAdditionalExpectedResponse
import org.eclipse.thingweb.thing.schema.WoTExpectedResponse
import org.eclipse.thingweb.thing.schema.WoTForm
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException

/**
 * A form can be viewed as a statement of "To perform an operation type operation on form context, make a request method request to submission target" where the optional form fields may further describe the required request. In Thing Descriptions, the form context is the surrounding Object, such as Properties, Actions, and Events or the Thing itself for meta-interactions.
 *
 * @property href Target IRI of a link or submission target of a form. Mandatory.
 * @property contentType Assign a content type based on a media type (e.g., text/plain) and potential parameters (e.g., charset=utf-8). Default is "text/plain".
 * @property contentCoding Content coding values indicate an encoding transformation that has been or can be applied to a representation. Examples include "gzip", "deflate", etc. Optional.
 * @property security Set of security definition names, chosen from those defined in securityDefinitions. These must all be satisfied for access to resources. Optional.
 * @property scopes Set of authorization scope identifiers provided as an array. These are provided in tokens returned by an authorization server and associated with forms to identify what resources a client may access and how. Optional.
 * @property response This optional term can be used if, e.g., the output communication metadata differ from input metadata (e.g., output contentType differs from the input contentType).
 * @property additionalResponses This optional term can be used if additional expected responses are possible, e.g., for error reporting. Each additional response needs to be distinguished from others in some way.
 * @property subprotocol Indicates the exact mechanism by which an interaction will be accomplished for a given protocol when there are multiple options (e.g., longpoll, websub, or sse). Optional.
 * @property op Indicates the semantic intention of performing the operation(s) described by the form. Default is "readproperty". Optional.
 */
@JsonInclude(NON_EMPTY)
data class Form (
    @JsonInclude(Include.ALWAYS)
    override val href: String, // Target IRI of a link or submission target of a form. Mandatory

    @JsonInclude(NON_EMPTY)
    override val contentType: String = "application/json", // Assign a content type. Default is "application/json"

    @JsonInclude(NON_EMPTY)
    override val contentCoding: String? = null, // Optional content coding values

    @JsonInclude(NON_EMPTY)
    override val security: List<String>? = null, // Optional set of security definition names

    @JsonInclude(NON_EMPTY)
    override val scopes: List<String>? = null, // Optional set of authorization scope identifiers

    @JsonInclude(NON_EMPTY)
    override val response: ExpectedResponse? = null, // Optional term for output communication metadata

    @JsonInclude(NON_EMPTY)
    override val additionalResponses: List<AdditionalExpectedResponse>? = null, // Optional additional expected responses

    @JsonInclude(NON_EMPTY)
    override val subprotocol: String? = null, // Optional term for the interaction mechanism

    @JsonInclude(NON_EMPTY)
    @JsonDeserialize(using = OperationsDeserializer::class)
    override val op: List<Operation>? = null, // Default op values

    @JsonInclude(NON_EMPTY)
    override val optionalProperties : MutableMap<String, Any> = mutableMapOf()
) : WoTForm {

    companion object {
        private val log = LoggerFactory.getLogger(Form::class.java)
    }

    @get:JsonIgnore
    val hrefScheme: String?
        get() = try {
            // remove uri variables first
            val sanitizedHref = href.substringBefore("{")
            URI(sanitizedHref).scheme
        } catch (e: URISyntaxException) {
            log.warn("Form href is invalid: ", e)
            null
        }

    // This method is called for any unknown fields during deserialization
    @JsonAnySetter
    fun setAdditionalProperties(key: String, value: Any) {
        optionalProperties[key] = value
    }


    @JsonInclude(NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ExpectedResponse(
        @get:JsonInclude(NON_EMPTY)
        override val contentType: String = "application/json", // Assign a content type based on a media type. Mandatory
        @get:JsonInclude(NON_EMPTY)
        override val description: String?
    ) : WoTExpectedResponse

    /**
     * Communication metadata describing the expected response message for additional responses.
     *
     * @property success Signals if an additional response should not be considered an error. Default is false.
     * @property contentType Assign a content type based on a media type (e.g., text/plain)
     * and potential parameters (e.g., charset=utf-8). Default is "text/plain".
     * @property schema Used to define the output data schema for an additional response
     * if it differs from the default output data schema. Optional.
     */
    @JsonInclude(NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AdditionalExpectedResponse(
        @get:JsonInclude(NON_EMPTY)
        override val success: Boolean = false, // Signals if an additional response should not be considered an error. Default is false.

        @get:JsonInclude(NON_EMPTY)
        override val contentType: String = "text/plain", // Assign a content type. Default is "text/plain".

        @get:JsonInclude(NON_EMPTY)
        override val schema: String? = null, // Used to define the output data schema for an additional response. Optional.
        @get:JsonInclude(NON_EMPTY)
        override val description: String? = null
    ) : WoTAdditionalExpectedResponse

}

