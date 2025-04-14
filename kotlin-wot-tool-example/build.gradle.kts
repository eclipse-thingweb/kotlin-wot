import org.springframework.boot.gradle.tasks.run.BootRun
import java.net.URI

plugins {
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.1.5" // Use the latest compatible version
    id("io.spring.dependency-management") version "1.1.3"
}

dependencies {
    api(project(":kotlin-wot-binding-http"))
    api(project(":kotlin-wot-binding-websocket"))
    api(project(":kotlin-wot-spring-boot-starter"))
    api(project(":kotlin-wot-lmos-protocol"))
    implementation("org.jsoup:jsoup:1.7.2")
}

springBoot {
    mainClass.set("org.eclipse.thingweb.example.ToolApplicationKt")
}

tasks.register("downloadOtelAgent") {
    doLast {
        val agentUrl =
            "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar"
        val agentFile = file("${project.buildDir}/libs/opentelemetry-javaagent.jar")

        // Ensure directory exists before downloading
        agentFile.parentFile.mkdirs()

        if (!agentFile.exists()) {
            println("Downloading OpenTelemetry Java Agent...")
            agentFile.writeBytes(URI(agentUrl).toURL().readBytes())
            println("Download completed: ${agentFile.absolutePath}")
        } else {
            println("OpenTelemetry Java Agent already exists: ${agentFile.absolutePath}")
        }
    }
}

tasks.named<BootRun>("bootRun") {
    dependsOn("downloadOtelAgent")
    jvmArgs = listOf(
        "-javaagent:${project.buildDir}/libs/opentelemetry-javaagent.jar"
    )
    systemProperty("otel.java.global-autoconfigure.enabled", "true")
    systemProperty("otel.traces.exporter", "otlp")
    systemProperty("otel.exporter.otlp.endpoint", "http://localhost:4318")
    systemProperty("otel.service.name", "scraper-tool")
    systemProperty("otel.javaagent.debug", "true")
}