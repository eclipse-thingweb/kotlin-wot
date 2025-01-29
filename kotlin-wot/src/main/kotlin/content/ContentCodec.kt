package ai.ancf.lmos.wot.content

import ai.ancf.lmos.wot.thing.schema.DataSchema
import com.fasterxml.jackson.databind.JsonNode
import kotlin.reflect.KClass

/**
 * A ContentCodec is responsible for (de)serializing data in certain encoding (e.g. JSON, CBOR).
 */
interface ContentCodec {

    /**
     * Returns the media type supported by the codec (e.g., `application/json`).
     *
     * @return The supported media type as a string.
     */
    val mediaType: String

    /**
     * Deserializes the given `body` according to the data schema defined in `schema`.
     *
     * This method calls the overloaded version of `bytesToValue`, providing an empty parameters map.
     *
     * @param body The byte array representing the encoded data.
     * @param schema The optional data schema defining the expected structure.
     * @return A `JsonNode` representing the deserialized value.
     * @throws ContentCodecException If deserialization fails.
     */
    fun bytesToValue(body: ByteArray, schema: DataSchema<*>?): JsonNode {
        return bytesToValue(body, schema, emptyMap())
    }

    /**
     * Deserializes the given `body` according to the data schema defined in `schema`.
     * The `parameters` map can contain additional metadata about the encoding, such as
     * the character set used.
     *
     * @param body The byte array representing the encoded data.
     * @param schema The optional data schema defining the expected structure.
     * @param parameters Additional encoding parameters (e.g., character set).
     * @return A `JsonNode` representing the deserialized value.
     * @throws ContentCodecException If deserialization fails.
     */
    fun bytesToValue(
        body: ByteArray,
        schema: DataSchema<*>?,
        parameters: Map<String, String>
    ): JsonNode

    /**
     * Deserializes the given `body` into an instance of type `O`.
     * The `parameters` map can contain additional metadata about the encoding.
     *
     * @param body The byte array representing the encoded data.
     * @param parameters Additional encoding parameters (e.g., character set).
     * @param <O> The target type of the deserialized value.
     * @return The deserialized object of type `O`.
     * @throws ContentCodecException If deserialization fails.
     */
    fun <O : Any> bytesToValue(body: ByteArray, parameters: Map<String, String>, clazz: KClass<O>): O

    /**
     * Serializes the given `value` according to the provided encoding parameters.
     *
     * @param value The object to serialize.
     * @param parameters Additional encoding parameters (e.g., character set).
     * @return A byte array representing the serialized data.
     * @throws ContentCodecException If serialization fails.
     */
    fun valueToBytes(value: Any, parameters: Map<String, String>): ByteArray

    /**
     * Serializes the given `JsonNode` according to the provided encoding parameters.
     *
     * @param value The `JsonNode` to serialize.
     * @param parameters Additional encoding parameters (e.g., character set).
     * @return A byte array representing the serialized JSON data.
     * @throws ContentCodecException If serialization fails.
     */
    fun valueToBytes(value: JsonNode, parameters: Map<String, String>): ByteArray
}
