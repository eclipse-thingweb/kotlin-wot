package ai.ancf.lmos.wot

import ai.ancf.lmos.wot.thing.ExposedThing
import ai.ancf.lmos.wot.thing.Thing
import ai.ancf.lmos.wot.thing.filter.DiscoveryMethod.*
import ai.ancf.lmos.wot.thing.filter.ThingFilter
import ai.ancf.lmos.wot.thing.form.Form
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
import java.util.concurrent.CompletableFuture

/**
 * The Servient hosts, exposes and consumes things based on provided protocol bindings.
 * https://w3c.github.io/wot-architecture/#sec-servient-implementation<br></br> It reads the servers
 * contained in the configuration parameter "wot.servient.servers", starts them and thus exposes
 * Things via the protocols supported by the servers. "wot.servient.servers" should contain an array
 * of strings of fully qualified class names implementing [ProtocolServer].<br></br> It also reads
 * the clients contained in the configuration parameter "wot.servient.client-factories" and is then
 * able to consume Things via the protocols supported by the clients.
 * "wot.servient.client-factories" should contain an array of strings of fully qualified class names
 * implementing [ProtocolClientFactory].<br></br> The optional configuration parameter
 * "wot.servient.credentials" can contain credentials (e.g. username and password) for the different
 * things.  The parameter should contain a map that uses the thing ids as key.
 */
class Servient(
    private val servers: List<ProtocolServer> = emptyList(),
    private val clients: List<ProtocolClient> = emptyList(),
    val things: MutableMap<String, ExposedThing> = mutableMapOf()
) {
    override fun toString(): String {
        return "Servient [servers=$servers clients=$clients]"
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
        val clientJobs = clients.map { client ->
            async { client.start(this@Servient) } // Launching client factory initialization in a coroutine
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
        val clientJobs = clients.map { client ->
            async { client.stop() } // Launching client factory initialization in a coroutine
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
     * @param exposedThing
     * @return
     */
    fun addThing(exposedThing: ExposedThing): Boolean {
        val previous: ExposedThing? = things.putIfAbsent(exposedThing.id, exposedThing)
        return previous == null
    }

    /**
     * Calls `url` and expects a Thing Description there. Returns the description as a
     * [Thing].
     *
     * @param url
     * @return
     */
    @Throws(URISyntaxException::class)
    fun fetch(url: String): Thing {
        return fetch(URI(url))
    }

    /**
     * Calls `url` and expects a Thing Description there. Returns the description as a
     * [Thing].
     *
     * @param url
     * @return
     */
    fun fetch(url: URI): Thing {
        log.debug("Fetch thing from url '{}'", url)
        val scheme = url.scheme
        return try {
            val client = getClientFor(scheme)
            if (client != null) {
                /*
                val form: Form = Builder()
                    .setHref(url.toString())
                    .build()
                client.readResource(form).thenApply { content ->
                    try {
                        val map: Map<*, *> = ContentManager.contentToValue(content, ObjectSchema())
                        return@thenApply Thing.fromMap(map)
                    } catch (e: ContentCodecException) {
                        throw CompletionException(ServientException("Error while fetching TD: $e"))
                    }
                }

                TODO
                */
                throw RuntimeException()
            } else {
               throw ServientException("Unable to fetch '$url'. Missing ClientFactory for scheme '$scheme'")
            }
        } catch (e: ProtocolClientException) {
            throw ServientException("Unable to create client: " + e.message)
        }
    }

    /**
     * Calls `url` and expects a Thing Directory there. Returns a list with all found
     * [Thing].
     *
     * @param url
     * @return
     * @throws URISyntaxException
     */
    @Throws(URISyntaxException::class)
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
    private suspend fun fetchDirectory(url: URI): Map<String, Thing> = coroutineScope {
        log.debug("Fetch thing directory from url '{}'", url)
        val scheme = url.scheme
        try {
            val client: ProtocolClient? = getClientFor(scheme)
            if (client != null) {
                val form = Form(url.toString())
                return@coroutineScope emptyMap()
                /*
                client.readResource(form).thenApply { content ->
                    try {
                        val value: Map<String, Map<*, *>> = ContentManager.contentToValue(content, ObjectSchema())
                        val directoryThings: MutableMap<String, Thing> = HashMap<String, Thing>()
                        if (value != null) {
                            for ((id, map) in value) {
                                val thing: Thing = Thing.fromMap(map)
                                directoryThings[id] = thing
                            }
                        }
                        return@thenApply directoryThings
                    } catch (e2: ContentCodecException) {
                        throw(ServientException("Error while fetching TD directory: $e2")
                    }

                    r
                }*/

            } else {
                throw ServientException("Unable to fetch directory '$url'. Missing ClientFactory for scheme '$scheme'")
            }
        } catch (e: ProtocolClientException) {
           throw ServientException("Unable to create client: " + e.message)
        }
    }

    private fun getClientFor(scheme: String) = clients.firstOrNull { client -> client.supports(scheme) }

    /**
     * Adds `thing` to the Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     * @throws URISyntaxException
     */
    @Throws(URISyntaxException::class)
    fun register(
        directory: String,
        thing: ExposedThing
    ): CompletableFuture<Void> {
        return register(URI(directory), thing)
    }

    /**
     * Adds `thing` to the Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     */
    private fun register(directory: URI, thing: ExposedThing): CompletableFuture<Void> {
        // FIXME: implement
        return CompletableFuture.failedFuture(ServientException("not implemented"))
    }

    /**
     * Removes `thing` from Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     * @throws URISyntaxException
     */
    @Throws(URISyntaxException::class)
    fun unregister(
        directory: String,
        thing: ExposedThing
    ): CompletableFuture<Void> {
        return unregister(URI(directory), thing)
    }

    /**
     * Removes `thing` from Thing Directory `directory`.
     *
     * @param directory
     * @param thing
     * @return
     */
    private fun unregister(directory: URI, thing: ExposedThing): CompletableFuture<Void> {
        // FIXME: implement
        return CompletableFuture.failedFuture(ServientException("not implemented"))
    }

    /**
     * Starts a discovery process for all available Things. Not all [ProtocolClient]
     * implementations support discovery. If none of the available clients support discovery, a
     * [ProtocolClientNotImplementedException] will be thrown.
     *
     * @return
     */
    @Throws(ServientException::class)
    suspend fun discover(): Flow<Thing> {
        return discover(ThingFilter(method = ANY))
    }

    /**
     * Starts a discovery process and searches for the things defined in `filter`. Not
     * all [ProtocolClient] implementations support discovery. If none of the available
     * clients support discovery, a [ProtocolClientNotImplementedException] will be thrown.
     *
     * @param filter
     * @return
     */
    @Throws(ServientException::class)
    suspend fun discover(filter: ThingFilter): Flow<Thing> {
        return when (filter.method) {
            DIRECTORY -> discoverDirectory(filter)
            LOCAL -> discoverLocal(filter)
            else -> discoverAny(filter)
        }
    }

    // Discover any available Things across all protocols
    @Throws(ServientException::class)
    private suspend fun discoverAny(filter: ThingFilter): Flow<Thing> = flow {
        var foundAtLeastOne = false
        // Try to run a discovery with every available protocol binding
        for (client in clients) {
            try {
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
    ): Flow<Thing> = flow {
        val discoveredThings: Map<String, Thing>? = filter.url?.let { fetchDirectory(it) }
        val thingsList: List<Thing> = discoveredThings?.values?.toList() ?: emptyList()

        // Apply the filter query if available
        val filteredThings: List<Thing>? = filter.query?.filter(thingsList)
        filteredThings?.forEach { emit(it) } // Emit each thing one by one
    }

    private fun discoverLocal(filter: ThingFilter): Flow<Thing> = flow {
        val myThings: List<Thing> = things.values
            .map { it }

        // Apply the filter query if available
        val filteredThings: List<Thing> = filter.query?.filter(myThings) ?: myThings
        filteredThings.forEach { emit(it) } // Emit each thing one by one
    }

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
            if (ifaceAddress.isLoopbackAddress() || ifaceAddress.isLinkLocalAddress() || ifaceAddress.isMulticastAddress()) {
                return null
            }
            return if (ifaceAddress is Inet4Address) {
                ifaceAddress.getHostAddress()
            } else if (ifaceAddress is Inet6Address) {
                var hostAddress: String = ifaceAddress.getHostAddress()

                // remove scope
                val percent = hostAddress.indexOf('%')
                if (percent != -1) {
                    hostAddress = hostAddress.substring(0, percent)
                }
                "[$hostAddress]"
            } else {
                null
            }
        }

    }
}

open class ServientException : WotException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String, cause: Exception): super(message, cause)
    constructor() : super()
}
