package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue

/**
 * A ContentCodec is responsible for (de)serializing data in certain encoding (e.g. JSON, CBOR).
 */
interface ContentCodec {

    /**
     * Returns the media type supported by the codec (e.g. application/json).
     *
     * @return
     */
    val mediaType: String

    /**
     * Deserializes `body` according to the data schema defined in `schema`.
     *
     * @param body
     * @param schema
     * @param <T>
     * @return
     * @throws ContentCodecException
    </T> */

    fun bytesToValue(body: ByteArray, schema: DataSchema<*>?): DataSchemaValue {
        return bytesToValue(body, schema, emptyMap())
    }

    /**
     * Deserializes `body` according to the data schema defined in `schema`.
     * `parameters` can contain additional information about the encoding of the data
     * (e.g. the used character set).
     *
     * @param body
     * @param schema
     * @param parameters
     * @param <T>
     * @return
     * @throws ContentCodecException
    </T> */
    fun bytesToValue(
        body: ByteArray,
        schema: DataSchema<*>?,
        parameters: Map<String, String>
    ): DataSchemaValue


    /**
     * Serialized `value` according to the data schema defined in `schema` to
     * a byte array. `parameters` can contain additional information about the encoding
     * of the data (e.g. the used character set).
     *
     * @param value
     * @param parameters
     * @return
     * @throws ContentCodecException
     */
    fun valueToBytes(value: Any, parameters: Map<String, String>): ByteArray

    fun valueToBytes(value: DataSchemaValue, parameters: Map<String, String>): ByteArray
}
