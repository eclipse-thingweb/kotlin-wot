package integration

import kotlinx.coroutines.runBlocking
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

fun main(): Unit = runBlocking {

    val hiveMqContainer = GenericContainer(DockerImageName.parse("hivemq/hivemq-ce:latest"))
        .withExposedPorts(1883)

    println("Starting HiveMQ container...")
    hiveMqContainer.start()

    println(hiveMqContainer.getMappedPort(1883))

    println("Application is running. Press Ctrl+C to stop.")

    // Register a shutdown hook for cleanup on termination
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Application is shutting down. Performing cleanup...")
        // Perform any necessary cleanup here
    })

    // Block the main thread indefinitely
    kotlinx.coroutines.suspendCancellableCoroutine<Unit> { /* Suspends forever */ }
}