package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.content.ContentCodecException
import ai.ancf.lmos.wot.content.ContentManager
import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.ThingDescription
import ai.ancf.lmos.wot.thing.filter.DiscoveryMethod.*
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.form.Form
import ai.ancf.lmos.wot.thing.schema.DataSchemaValue
import ai.ancf.lmos.wot.thing.schema.ObjectSchema
import ai.ancf.lmos.wot.thing.schema.WoTExposedThing
import ai.anfc.lmos.wot.binding.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import java.net.*
import java.util.*

/**
 * The Servient hosts, exposes and consumes things based on provided protocol bindings.
 * https://w3c.github.io/wot-architecture/#sec-servient-implementation
 */
class Servient(
    private val servers: List<ProtocolServer> = emptyList(),
    clientFactories: List<ProtocolClientFactory> = emptyList(),
    val things: MutableMap<String, ExposedThing> = mutableMapOf()
) {
    private val clientFactories: Map<String, ProtocolClientFactory> = clientFactories.associateBy { it.scheme }

    override fun toString(): String {
        return "Servient [servers=" + servers + " clientFactories=" + clientFactories.values + "]"
    }

    /**
     * Launch the servient. All servers supported by the servient (e.g. HTTP, CoAP, ...) are
     * started. The servers are then ready to accept requests for the exposed Things.
     *
     * @return
     */
    suspend fun start() : Unit = coroutineScope {
        log.info("Start Servient")

        // Launch coroutines for starting servers
        val serverJobs = servers.map { server ->
            async { server.start(this@Servient) } // Launching each server start in a coroutine
        }

        // Launch coroutines for initializing client factories
        val clientJobs = clientFactories.values.map { client ->
            async { client.init() } // Launching client factory initialization in a coroutine
        }

        // Wait for all jobs to complete
        (serverJobs + clientJobs).awaitAll() // Awaits completion of all the coroutines
    }

    /**
     * Shut down the servient. All servers supported by the servient (e.g. HTTP, CoAP, ...) are shut
     * down. Interaction with exposed Things is then no longer possible.
     *
     * @return
     */
    suspend fun shutdown() : Unit = coroutineScope {
        log.info("Stop Servient")

        // Launch coroutines for starting servers
        val serverJobs = servers.map { server ->
            async { server.stop() } // Launching each server start in a coroutine
        }

        // Launch coroutines for initializing client factories
        val clientJobs = clientFactories.values.map { client ->
            async { client.destroy() } // Launching client factory initialization in a coroutine
        }

        // Wait for all jobs to complete
        (serverJobs + clientJobs).awaitAll() // Awaits completion of all the coroutines
    }

    /**
     * All servers supported by Servient are instructed to expose the Thing with the given
     * `id`. Then it is possible to interact with the Thing via different protocols
     * (e.g. HTTP, CoAP, ...). Before a thing can be exposed, it must be added via [ ][.addThing].
     *
     * @param id
     * @return
     */
    suspend fun expose(id: String) : ExposedThing = coroutineScope {
        val thing: ExposedThing? = things[id]
        if (servers.isEmpty()) {
            throw ServientException("Servient has no servers to expose Things")
        }
        if (thing == null) {
            throw ServientException("Thing must be added to the servient first")
        }
        log.info("Servient exposing '{}'", id)

        val serverJobs = servers.map { server ->
            async { server.expose(thing) } // Launching each server start in a coroutine
        }

        (serverJobs).awaitAll() // Awaits completion of all the coroutines
        thing
    }

    /**
     * All servers supported by Servient are instructed to stop exposing the Thing with the given
     * `id`. After that no further interaction with the thing is possible.
     *
     * @param id
     * @return
     */
    suspend fun destroy(id: String) : ExposedThing = coroutineScope {
        val thing: ExposedThing? = things[id]
        if (servers.isEmpty()) {
            throw ServientException("Servient has no servers to stop exposure Things")
        }
        if (thing == null) {
            throw ServientException("Thing must be added to the servient first")
        }
        log.info("Servient stop exposing '{}'", thing)

        // TODO reset forms

        val serverJobs = servers.map { server ->
            async { server.destroy(thing) } // Launching each server start in a coroutine
        }

        (serverJobs).awaitAll() // Awaits completion of all the coroutines
        thing
    }

    /**
     * Adds `thing` to the servient. This allows the Thing to be exposed later.
     *
     * @param thing
     * @return
     */
    fun addThing(thing: WoTExposedThing): Boolean {
        thing as ExposedThing
        val previous = things.putIfAbsent(thing.id, thing)
        return previous == null
    }

    /**
     * Calls `url` and expects a Thing Description there. Returns the description as a
     * [ThingDescription].
     *
     * @param url
     * @return
     */
    suspend fun fetch(url: String): ThingDescription {
        return fetch(URI(url))
    }

    /**
     * Calls `url` and expects a Thing Description there. Returns the description as a
     * [ThingDescription].
     *
     * @param url
     * @return
     */
     suspend fun fetch(url: URI): ThingDescription {
        log.debug("Fetch thing from url '{}'", url)
        val scheme = url.scheme
        try {
            val client = getClientFor(scheme)
            if (client != null) {
                val form = Form(href = url.toString())
                val content = client.readResource(form)
                try {
                    val dataSchemaValue = ContentManager.contentToValue(content, ObjectSchema())
                    dataSchemaValue as DataSchemaValue.ObjectValue

                    return ThingDescription.fromMap(dataSchemaValue.value)
                } catch (e: ContentCodecException) {
                    throw ServientException("Error while fetching TD: ${e.message}", e)
                }
            } else {
                throw ServientException("Unable to fetch '$url'. Missing ClientFactory for scheme '$scheme'")
            }
        } catch (e: ProtocolClientException) {
            throw ServientException("Unable to create client: ${e.message}", e)
        }
    }


    fun getCredentials(id: String?): String {
        log.debug("Servient looking up credentials for '{}'", id)
        return "credentialStore.get(id)"
    }

    /*

    /**
     * Calls `url` and expects a Thing Directory there. Returns a list with all found
     * [Thing].
     *
     * @param url
     * @return
     * @throws URISyntaxException
     */
    suspend fun fetchDirectory(url: String):Map<String, Thing> {
        return fetchDirectory(URI(url))
    }

    /**
     * Calls `url` and expects a Thing Directory there. Returns a list with all found
     * [Thing].
     *
     * @param url
     * @return
     */
    suspend fun fetchDirectory(url: URI): Map<String, Thing> {
        log.debug("Fetch thing directory from url '{}'", url)
        val scheme = url.scheme
        try {
            val client: ProtocolClient? = getClientFor(scheme)
            if (client != null) {
                val form = Form(url.toString())
                val content = client.readResource(form)
                try {
                    val things = ContentManager.contentToValue(content, ArraySchema<Map<*,*>>())
                    val directoryThings: MutableMap<String, Thing> = mutableMapOf()

                    for (thingMap in things) {
                        val thing = thingMap as Thing
                        directoryThings[thing.id] = thing
                    }

                    return directoryThings
                } catch (e2: ContentCodecException) {
                    throw ServientException("Error while fetching TD directory: $e2")
                }
            } else {
                throw ServientException("Unable to fetch directory '$url'. Missing ClientFactory for scheme '$scheme'")
            }
        } catch (e: ProtocolClientException) {
           throw ServientException("Unable to create client: " + e.message)
        }
    }

     */

    fun getClientFor(scheme: String): ProtocolClient? {
        val factory = clientFactories[scheme]
        return if (factory != null) {
            factory.client
        } else {
            log.warn("Servient has no ClientFactory for scheme '{}'", scheme)
            null
        }
    }

    /**
     * Adds `thing` to the Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     * @throws URISyntaxException
     */

    fun register(
        directory: String,
        thing: ExposedThing
    ) {
        return register(URI(directory), thing)
    }

    /**
     * Adds `thing` to the Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     */
    private fun register(directory: URI, thing: ExposedThing) {
        // FIXME: implement
        throw ServientException("not implemented")
    }

    /**
     * Removes `thing` from Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     * @throws URISyntaxException
     */

    fun unregister(
        directory: String,
        thing: ExposedThing
    ) {
        return unregister(URI(directory), thing)
    }

    /**
     * Removes `thing` from Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     */
    private fun unregister(directory: URI, thing: ExposedThing) {
        throw ServientException("not implemented")
    }

    /**
     * Starts a discovery process for all available Things. Not all [ProtocolClient]
     * implementations support discovery. If none of the available clients support discovery, a
     * [ProtocolClientNotImplementedException] will be thrown.
     *
     * @return
     */

    suspend fun discover(): Flow<WoTExposedThing> {
        return discover(ThingFilter(method = ANY))
    }

    fun getClientSchemes(): Set<String> {
        return clientFactories.keys.toMutableSet()
    }

    /**
     * Starts a discovery process and searches for the things defined in `filter`. Not
     * all [ProtocolClient] implementations support discovery. If none of the available
     * clients support discovery, a [ProtocolClientNotImplementedException] will be thrown.
     *
     * @param filter
     * @return
     */

    suspend fun discover(filter: ThingFilter): Flow<WoTExposedThing> {
        return when (filter.method) {
            DIRECTORY -> discoverDirectory(filter)
            LOCAL -> discoverLocal(filter)
            else -> discoverAny(filter)
        }
    }

    // Discover any available Things across all protocols
    @Throws(ServientException::class)
    private suspend fun discoverAny(filter: ThingFilter): Flow<WoTExposedThing> = flow {
        var foundAtLeastOne = false
        // Try to run a discovery with every available protocol binding
        for (factory in clientFactories.values) {
            try {
                val client = factory.client
                val clientFlow = client.discover(filter) // Assuming discover now returns Flow
                emitAll(clientFlow) // Merges the flow from the client
                foundAtLeastOne = true
            } catch (e: ProtocolClientNotImplementedException) {
                // If client does not implement discovery, we ignore and move on
            }
        }

        // Fail if none of the available protocol bindings support discovery
        if (!foundAtLeastOne) {
            throw ProtocolClientNotImplementedException("None of the available clients implements 'discovery'.")
        }

        // Merge with local discovery
        emitAll(discoverLocal(filter))
    }

    private suspend fun discoverDirectory(
        filter: ThingFilter
    ): Flow<ExposedThing> = flow {
        //val discoveredThings = filter.url?.let { fetchDirectory(it) } TODO
        val discoveredThings : Map<String, ExposedThing>? = null
        val thingsList = discoveredThings?.values?.toList() ?: emptyList()

        // Apply the filter query if available
        val filteredThings = filter.query?.filter(thingsList)
        filteredThings?.forEach { emit(it) } // Emit each thing one by one
    }

    private fun discoverLocal(filter: ThingFilter): Flow<WoTExposedThing> = flow {
        val myThings = things.values.toList()

        // Apply the filter query if available
        val filteredThings = filter.query?.filter(myThings) ?: myThings
        filteredThings.forEach { emit(it) } // Emit each thing one by one
    }

    fun hasClientFor(scheme: String): Boolean = clientFactories.containsKey(scheme)

    companion object {
        private val log = LoggerFactory.getLogger(Servient::class.java)
        val addresses: Set<String>
            /**
             * Returns a list of the IP addresses of all network interfaces of the local computer. If no IP
             * addresses can be obtained, 127.0.0.1 is returned.
             *
             * @return
             */
            get() = try {
                val addresses: MutableSet<String> = HashSet()
                val ifaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
                while (ifaces.hasMoreElements()) {
                    val iface: NetworkInterface = ifaces.nextElement()
                    if (!iface.isUp || iface.isLoopback || iface.isPointToPoint) {
                        continue
                    }
                    val ifaceAddresses: Enumeration<InetAddress> = iface.inetAddresses
                    while (ifaceAddresses.hasMoreElements()) {
                        val ifaceAddress: InetAddress = ifaceAddresses.nextElement()
                        val address = getAddressByInetAddress(ifaceAddress)
                        if (address != null) {
                            addresses.add(address)
                        }
                    }
                }
                addresses
            } catch (e: SocketException) {
                HashSet<String>(listOf<String>("127.0.0.1"))
            }

        private fun getAddressByInetAddress(ifaceAddress: InetAddress): String? {
            if (ifaceAddress.isLoopbackAddress || ifaceAddress.isLinkLocalAddress || ifaceAddress.isMulticastAddress) {
                return null
            }
            return when (ifaceAddress) {
                is Inet4Address -> {
                    ifaceAddress.getHostAddress()
                }

                is Inet6Address -> {
                    var hostAddress: String = ifaceAddress.getHostAddress()

                    // remove scope
                    val percent = hostAddress.indexOf('%')
                    if (percent != -1) {
                        hostAddress = hostAddress.substring(0, percent)
                    }
                    "[$hostAddress]"
                }

                else -> {
                    null
                }
            }
        }

    }
}

open class ServientException : WotException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String, cause: Throwable): super(message, cause)
    constructor() : super()
}
