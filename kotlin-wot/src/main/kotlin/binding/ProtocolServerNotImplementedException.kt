package ai.anfc.lmos.wot.binding


/**
 * This exception is thrown when the a [ProtocolServer] implementation does not support a
 * requested functionality.
 */
class ProtocolServerNotImplementedException : ProtocolServerException {
    constructor(
        clazz: Class<*>,
        operation: String
    ) : super(clazz.getSimpleName() + " does not implement '" + operation + "'")

    constructor(message: String?) : super(message)
}
