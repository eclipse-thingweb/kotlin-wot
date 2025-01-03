package ai.ancf.lmos.wot.content

/**
 * Represents any serialized content. Enables the transfer of arbitrary data structures.
 */
data class Content(val type: String = ContentManager.DEFAULT_MEDIA_TYPE, val body: ByteArray = ByteArray(0)) {

    companion object {
        val EMPTY_CONTENT = Content(ContentManager.DEFAULT_MEDIA_TYPE, ByteArray(0))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Content

        if (type != other.type) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }
}

fun String.toJsonContent(): Content{
    val jsonContent = """"$this""""
    return Content(ContentManager.DEFAULT_MEDIA_TYPE, jsonContent.toByteArray())
}
