/*
 * SPDX-FileCopyrightText: Robert Winkler
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.ServientException
import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BinaryNode
import org.slf4j.LoggerFactory
import java.io.*
import kotlin.reflect.KClass


object ContentManager {
    const val DEFAULT_MEDIA_TYPE = "application/json"
    private val log = LoggerFactory.getLogger(ContentManager::class.java)
    private val CODECS: MutableMap<String, ContentCodec> = mutableMapOf()
    private val OFFERED: MutableSet<String> = HashSet()

    init {
        addCodec(JsonCodec(), true)
        //addCodec(LinkFormatCodec())
    }

    /**
     * Adds support for media type specified in codec.
     *
     * @param codec
     */
    private fun addCodec(codec: ContentCodec) {
        addCodec(codec, false)
    }

    /**
     * Adds support for media type specified in codec. If offered is `true`, this media type
     * will also be included in thing descriptions.
     *
     * @param codec
     * @param offered
     */
    internal fun addCodec(codec: ContentCodec, offered: Boolean) {
        CODECS[codec.mediaType] = codec
        if (offered) {
            OFFERED.add(codec.mediaType)
        }
    }

    /**
     * Removes support for media type specified in codec.
     *
     * @param mediaType
     */
    fun removeCodec(mediaType: String) {
        CODECS.remove(mediaType)
        OFFERED.remove(mediaType)
    }

    val offeredMediaTypes: Set<String>
        /**
         * Servient will offer these media types in the thing descriptions.
         *
         * @return
         */
        get() = OFFERED

    fun <O : Any> contentToValue(content: Content, kClass: KClass<O>): O {
        // Get content type or use default
        val contentType = content.type

        val mediaType = getMediaType(contentType)
        val parameters = getMediaTypeParameters(contentType)

        // Choose codec based on media type
        val codec = findCodec(mediaType)
        return if (codec != null) {
            log.debug("Content deserializing from '$mediaType'")
            codec.bytesToValue(content.body, parameters, kClass)
        }
        else {
            throw ContentCodecException("Unable to deserialize content because codec not supported")
        }
    }

    /**
     * Deserializes `content` according to the data schema defined in
     * `schema`. Returns `null` if no schema is specified. If
     * `content` does not define a content type, [.DEFAULT] is assumed. If the
     * content type defined in `content` is not supported, Java's internal serialization
     * method will be used.
     *
     * @param content
     * @param schema
     * @param <T>
     * @return
     * @throws ContentCodecException
    </T> */
    fun contentToValue(
        content: Content,
        schema: DataSchema<*>?
    ): JsonNode {
        // Get content type or use default
        val contentType = content.type

        val mediaType = getMediaType(contentType)
        val parameters = getMediaTypeParameters(contentType)

        // Choose codec based on media type
        val codec = findCodec(mediaType)
        return if (codec != null) {
            log.debug("Content deserializing from '$mediaType'")
            codec.bytesToValue(content.body, schema, parameters)
        }else {
            throw ContentCodecException("Unable to deserialize content because codec not supported")
        }
    }

    // Add a function to resolve a matching codec
    private fun findCodec(contentType: String): ContentCodec? {
        // Exact match
        CODECS[contentType]?.let { return it }

        // Attempt to find a compatible codec
        val normalizedType = normalizeContentType(contentType)
        return CODECS.keys.firstOrNull { key ->
            normalizeContentType(key) == normalizedType
        }?.let { CODECS[it] }
    }

    // Helper to normalize content types
    fun normalizeContentType(contentType: String): String {
        val mainType = contentType.substringBefore('/')
        val subType = contentType.substringAfter('/')

        // Check for a structured suffix (e.g., "+json") and retain it
        val baseSubType = subType.substringBefore('+')
        val suffix = subType.substringAfter('+', "")

        return if (suffix.isNotEmpty()) {
            "$mainType/$suffix"
        } else {
            "$mainType/$baseSubType"
        }
    }

    /**
     * Extracts the media type from `contentType` (e.g. "text/plain; charset=utf-8"
     * becomes "text/plain"). Returns `null` if no media type could be found.
     *
     * @param contentType
     * @return
     */
     fun getMediaType(contentType: String?): String {
        if (contentType == null) {
            return DEFAULT_MEDIA_TYPE
        }
        val parts = contentType.split(";".toRegex(), limit = 2).toTypedArray()
        return parts[0].trim { it <= ' ' }
    }

    /**
     * Returns a [Map] with all media type parameters in `contentType` (e.g.
     * "text/plain; charset=utf-8" results in a one-element map with "charset" as key and "utf-8" as
     * value). Returns `null` if no media type could be found.
     *
     * @param contentType
     * @return
     */
    internal fun getMediaTypeParameters(contentType: String?): Map<String, String> {
        // Return an empty map if contentType is null
        if (contentType == null) {
            return emptyMap()
        }

        return contentType.split(";")
            .drop(1).associate { part ->
                val (name, value) = part.split("=", limit = 2).map { it.trim() }
                name to value // Create a Pair
            } // Convert the list of pairs to a map
    }



    private fun fallbackBytesToValue(
        content: Content
    ): JsonNode {
         try {
             return BinaryNode(content.body)
        } catch (e: IOException) {
            throw ContentCodecException("Unable to deserialize content: " + e.message, e)
        } catch (e: ClassNotFoundException) {
            throw ContentCodecException("Unable to deserialize content: " + e.message, e)
        }
    }



    /**
     * Serialized `value` using default content type defined in [.DEFAULT] to a
     * [Content] object.
     *
     * @param value
     * @return
     * @throws ContentCodecException
     */
    fun valueToContent(value: Any?): Content {
        return valueToContent(value, null)
    }

    fun valueToContent(value: JsonNode, contentType: String?): Content {
        val mediaType = getMediaType(contentType ?: DEFAULT_MEDIA_TYPE)
        val parameters = getMediaTypeParameters(contentType ?: DEFAULT_MEDIA_TYPE)

        // Select codec based on mediaType and log the action
        val codec = CODECS[mediaType]
        val bytes: ByteArray = if (codec != null) {
            log.debug("Content serializing to '$mediaType'")
            codec.valueToBytes(value, parameters)
        } else {
            log.warn("Content passthrough due to unsupported serialization format '$mediaType'")
            fallbackValueToBytes(value)
        }
        return Content(contentType ?: DEFAULT_MEDIA_TYPE, bytes)
    }

    /**
     * Serialized `value` according to the content type defined in
     * `contentType` to a [Content] object. If the content type defined in
     * `contentType` is not supported, Java's internal serialization method will be
     * used.
     *
     * @param value
     * @param contentType
     * @return
     * @throws ContentCodecException
     */
    fun valueToContent(value: Any?, contentType: String?): Content {
        // Use a default value for contentType if null
        val mediaType = getMediaType(contentType ?: DEFAULT_MEDIA_TYPE)
        val parameters = getMediaTypeParameters(contentType ?: DEFAULT_MEDIA_TYPE)

        // Select codec based on mediaType and log the action
        val codec = CODECS[mediaType]
        val bytes: ByteArray = if (codec != null) {
            log.debug("Content serializing to '$mediaType'")
            codec.valueToBytes(value ?: throw IllegalArgumentException("Value cannot be null when codec is available."), parameters)
        } else {
            log.warn("Content passthrough due to unsupported serialization format '$mediaType'")
            fallbackValueToBytes(value)
        }

        return Content(contentType ?: DEFAULT_MEDIA_TYPE, bytes)
    }

    private fun fallbackValueToBytes(value: Any?): ByteArray {
        val bytes: ByteArray = try {
            val byteStream = ByteArrayOutputStream()
            val objectStream = ObjectOutputStream(byteStream)
            objectStream.writeObject(value)
            objectStream.flush()
            byteStream.toByteArray()
        } catch (e: IOException) {
            throw ContentCodecException("Unable to serialize content: " + e.message, e)
        }
        return bytes
    }

    /**
     * Returns `true` if data in the format `contentType` can be
     * (de)serialized. Otherwise `false` is returned.
     *
     * @param contentType
     * @return
     */
    fun isSupportedMediaType(contentType: String?): Boolean {
        val mediaType = getMediaType(contentType)
        return CODECS.keys.contains(mediaType)
    }



    private class SecureObjectInputStream<T>(`in`: InputStream?, val schema: DataSchema<T>) : ObjectInputStream(`in`) {
        override fun resolveClass(desc: ObjectStreamClass): Class<*> {
            if (!isAllowedClass(desc.name)) {
                throw InvalidClassException("Disallowed class for DataSchema '$schema': ", desc.name)
            }
            return super.resolveClass(desc)
        }

        private fun isAllowedClass(className: String): Boolean {
            return schema.classType.isAssignableFrom(Class.forName(className))
        }
    }
}


class ContentCodecException : ServientException {
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)
}


