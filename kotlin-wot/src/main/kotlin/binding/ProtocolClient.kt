package ai.anfc.lmos.wot.binding

import ai.ancf.lmos.wot.content.Content
import ai.ancf.lmos.wot.security.SecurityScheme
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.ThingDescription
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.form.Form
import kotlinx.coroutines.flow.Flow

/**
 * A ProtocolClient defines how to interact with a Thing via a specific protocol (e.g. HTTP, MQTT,
 * etc.).
 */
interface ProtocolClient {

    /**
     * Adds the `metadata` with security mechanisms (e.g. use password authentication)
     * and `credentials`credentials (e.g. password and username) of a things to the
     * client.
     *
     * @param metadata
     * @param credentials
     * @return
     */
    fun setSecurity(metadata: List<SecurityScheme>, credentials: Map<String, String>): Boolean {
        return false
    }

    /**
     * Starts the discovery process that will provide Things that match the `filter`
     * argument.
     *
     * @param filter
     * @return
     */
    fun discover(filter: ThingFilter): Flow<ExposedThing> {
        throw ProtocolClientNotImplementedException(javaClass, "discover")
    }

    /**
     * Reads the resource defined in `form`. This can be a [ThingProperty], a
     * [ThingDescription] or a Thing Directory.
     *
     * @param form
     * @return
     */
    suspend fun readResource(form: Form): Content {
        throw ProtocolClientNotImplementedException(javaClass, "read")
    }

    /**
     * Writes `content` to the resource defined in `form`. This can be, for
     * example, a [ThingProperty].
     *
     * @param form
     * @param content
     * @return
     */
    suspend fun writeResource(form: Form, content: Content) {
        throw ProtocolClientNotImplementedException(javaClass, "write")
    }

    /**
     * Invokes the resource defined in the `form`. This can be a [ThingAction], for
     * example.
     *
     * @param form
     * @return
     */
    suspend fun invokeResource(form: Form): Content {
        return invokeResource(form, null)
    }

    /**
     * Invokes the resource defined in the `form` with the payload defined in
     * `content`. This can be a [ThingAction], for
     * example.
     *
     * @param form
     * @param content
     * @return
     */
    suspend fun invokeResource(form: Form, content: Content?): Content {
        throw ProtocolClientNotImplementedException(javaClass, "invoke")
    }

    /**
     * Create an observable for the resource defined in `form`. This resource can be, for
     * example, an [ThingEvent] or an observable [ThingProperty].
     *
     * @param form
     * @return
     */
    suspend fun observeResource(form: Form): Flow<Content> {
        throw ProtocolClientNotImplementedException(javaClass, "observe")
    }
}