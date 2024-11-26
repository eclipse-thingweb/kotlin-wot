package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.ServientException
import ai.ancf.lmos.wot.thing.schema.ArraySchema
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import ai.ancf.lmos.wot.thing.schema.ObjectSchema
import org.slf4j.LoggerFactory
import java.io.*


object ContentManager {
    const val DEFAULT = "application/json"
    private val log = LoggerFactory.getLogger(ContentManager::class.java)
    private val CODECS: MutableMap<String, ContentCodec> = mutableMapOf()
    private val OFFERED: MutableSet<String> = HashSet()

    init {
        addCodec(JsonCodec(), true)
        addCodec(TextCodec())
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
    ): DataSchemaValue {
        // Get content type or use default
        val contentType = content.type

        val mediaType = getMediaType(contentType)
        val parameters = getMediaTypeParameters(contentType)

        // Choose codec based on media type
        val codec = CODECS[mediaType]
        return if (codec != null) {
            log.debug("Content deserializing from '$mediaType'")
            codec.bytesToValue(content.body, schema, parameters)
        }
        else {
            log.warn("Content passthrough due to unsupported media type '$mediaType'")
            fallbackBytesToValue(content, schema!!)
        }
    }

    /**
     * Extracts the media type from `contentType` (e.g. "text/plain; charset=utf-8"
     * becomes "text/plain"). Returns `null` if no media type could be found.
     *
     * @param contentType
     * @return
     */
    internal fun getMediaType(contentType: String?): String? {
        if (contentType == null) {
            return null
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



    private fun <T> fallbackBytesToValue(
        content: Content,
        schema: DataSchema<T>
    ): DataSchemaValue {
         try {
             val byteStream = ByteArrayInputStream(content.body)
             val objectStream: ObjectInputStream = SecureObjectInputStream(byteStream, schema)
             val response = when (val readObject = objectStream.readObject()) {
                is String -> {
                    DataSchemaValue.StringValue(readObject)
                }
                is Int -> {
                    // Parse as IntegerValue
                    DataSchemaValue.IntegerValue(readObject)
                }
                is Number -> {
                    // Parse as NumberValue
                    DataSchemaValue.NumberValue(readObject)
                }
                is Boolean -> {
                    // Parse as BooleanValue
                    DataSchemaValue.BooleanValue(readObject)
                }
                is ArraySchema<*> -> {
                    // Parse as ArrayValue
                    DataSchemaValue.ArrayValue(readObject as List<*>)
                }
                is ObjectSchema -> {
                    // Parse as ObjectValue
                    DataSchemaValue.ObjectValue(readObject as Map<*, *>)
                }
                else -> { DataSchemaValue.NullValue }
            }
             return response
        } catch (e: IOException) {
            throw ContentCodecException("Unable to deserialize content: " + e.message)
        } catch (e: ClassNotFoundException) {
            throw ContentCodecException("Unable to deserialize content: " + e.message)
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

    fun valueToContent(value: DataSchemaValue, contentType: String?): Content {
        val mediaType = getMediaType(contentType ?: DEFAULT)
        val parameters = getMediaTypeParameters(contentType ?: DEFAULT)

        // Select codec based on mediaType and log the action
        val codec = CODECS[mediaType]
        val bytes: ByteArray = if (codec != null) {
            log.debug("Content serializing to '$mediaType'")
            codec.valueToBytes(value, parameters)
        } else {
            log.warn("Content passthrough due to unsupported serialization format '$mediaType'")
            fallbackValueToBytes(value)
        }
        return Content(contentType ?: DEFAULT, bytes)
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
        val mediaType = getMediaType(contentType ?: DEFAULT)
        val parameters = getMediaTypeParameters(contentType ?: DEFAULT)

        // Select codec based on mediaType and log the action
        val codec = CODECS[mediaType]
        val bytes: ByteArray = if (codec != null) {
            log.debug("Content serializing to '$mediaType'")
            codec.valueToBytes(value ?: throw IllegalArgumentException("Value cannot be null when codec is available."), parameters)
        } else {
            log.warn("Content passthrough due to unsupported serialization format '$mediaType'")
            fallbackValueToBytes(value)
        }

        return Content(contentType ?: DEFAULT, bytes)
    }

    private fun fallbackValueToBytes(value: Any?): ByteArray {
        val bytes: ByteArray = try {
            val byteStream = ByteArrayOutputStream()
            val objectStream = ObjectOutputStream(byteStream)
            objectStream.writeObject(value)
            objectStream.flush()
            byteStream.toByteArray()
        } catch (e: IOException) {
            throw ContentCodecException("Unable to serialize content: " + e.message)
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


