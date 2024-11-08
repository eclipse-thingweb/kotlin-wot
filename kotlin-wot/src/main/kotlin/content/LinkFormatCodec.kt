package ai.ancf.lmos.wot.content

/*
class LinkFormatCodec : ContentCodec {
    private val log = LoggerFactory.getLogger(LinkFormatCodec::class.java)
    override val mediaType: String
        get() = "application/link-format"

    override fun <T> bytesToValue(
        body: ByteArray,
        schema: DataSchema<T>,
        parameters: Map<String, String>
    ): T {
        val pattern = Regex("""^(.+)="(.+)"""")

        return when (schema) {
            is ObjectSchema -> {
                val entries = mutableMapOf<String?, Map<String, String>>()
                val bodyString = body.toString(Charsets.UTF_8) // Decode the byte array to a string
                val entriesStrings = bodyString.split(",").filter { it.isNotEmpty() } // Split and filter empty entries

                for (entryString in entriesStrings) {
                    val entryComponents = entryString.split(";", limit = 4).toMutableList()
                    val entryKey = entryComponents.removeAt(0)
                    val entryParameters = mutableMapOf<String, String>()

                    entryComponents.forEach { component ->
                        val matchResult = pattern.matchEntire(component)
                        matchResult?.let {
                            val key = it.groups[1]?.value
                            val value = it.groups[2]?.value
                            if (key != null && value != null) {
                                entryParameters[key] = value
                            }
                        }
                    }
                    entries[entryKey] = entryParameters
                }
                entries as T // Safe cast, since we know the structure
            }
            else -> throw ContentCodecException("Non-object data schema not implemented yet")
        }
    }

    override fun valueToBytes(value: Any, parameters: Map<String, String>): ByteArray {
        return if (value is Map<*, *>) {
            val valueMap = value as Map<String, Map<String, String>>
            val bodyString = valueMap.entries.stream().map { (key, value1): Map.Entry<String, Map<String, String>> ->
                "$key;" + value1.entries.stream()
                    .map { (key1, value2): Map.Entry<String, String> -> "$key1=\"$value2\"" }
                    .collect(Collectors.joining(";"))
            }.collect(Collectors.joining(","))
            bodyString.toByteArray()
        } else {
            log.warn("Unable to serialize non-map value: {}", value)
            ByteArray(0)
        }
    }
}
*/
