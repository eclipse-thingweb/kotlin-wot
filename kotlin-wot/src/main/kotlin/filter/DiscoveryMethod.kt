package ai.ancf.lmos.wot.thing.filter

/**
 * Defines "Where" to search for things during a discovery process.
 */
enum class DiscoveryMethod {
    /**
     * Uses the discovery mechanisms provided by all [ProtocolClient] implementations to
     * consider all available Things.
     */
    ANY,

    /**
     * Searches only on the local [Servient].
     */
    LOCAL,

    /**
     * Is used together with a URL to search in a specific Thing Directory.
     */
    DIRECTORY
    //    MULTICAST
}
