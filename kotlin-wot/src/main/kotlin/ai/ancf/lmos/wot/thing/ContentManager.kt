package ai.ancf.lmos.wot.ai.ancf.lmos.wot.thing

import ai.ancf.lmos.wot.ServientException
import ai.ancf.lmos.wot.thing.schema.DataSchema
import ai.anfc.lmos.wot.binding.Content

interface ContentManager {
    companion object {
        fun <T> contentToValue(result: Content, dataSchema: DataSchema<T>?): T {
            TODO("Not yet implemented")
        }

        fun valueToContent(parameters: Map<String, Any>, contentType: String): Content {
            TODO("Not yet implemented")
        }
    }

}


class ContentCodecException : ServientException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}


