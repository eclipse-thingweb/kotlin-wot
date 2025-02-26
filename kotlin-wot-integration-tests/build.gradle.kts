
import org.springframework.boot.gradle.tasks.run.BootRun
import java.net.URI

plugins {
    kotlin("plugin.spring") version "1.9.10"
    id("org.springframework.boot") version "3.1.5" // Use the latest compatible version
    id("io.spring.dependency-management") version "1.1.3"
}

tasks.named<Test>("test") {
    enabled = false
}

dependencies {
    // Replace the following with the starter dependencies of specific modules you wish to use
    api(project(":kotlin-wot-binding-http"))
    api(project(":kotlin-wot-binding-websocket"))
    api(project(":kotlin-wot-binding-mqtt"))
    api(project(":kotlin-wot-spring-boot-starter"))
    api(project(":kotlin-wot-lmos-protocol"))
    implementation("org.eclipse.lmos:arc-azure-client:0.1.0-SNAPSHOT")

    api("org.eclipse.lmos:arc-spring-boot-starter:0.1.0-SNAPSHOT")

    //implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:2.13.1")
    //implementation("com.azure:azure-core-metrics-opentelemetry:1.0.0-beta.27")
    //implementation("com.azure:azure-core-tracing-opentelemetry:1.0.0-beta.55")
    //implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.13.1")
    implementation("io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.13.1-alpha")
    //implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
    //implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // AspectJ runtime for annotation-based AOP
    //implementation("org.aspectj:aspectjrt:1.9.9")

    // AspectJ weaver for load-time weaving (LTW)
    //implementation("org.aspectj:aspectjweaver:1.9.9")
    //implementation("io.micrometer:micrometer-tracing-bridge-otel")
    //implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    implementation("dev.langchain4j:langchain4j-azure-open-ai:1.0.0-beta1")
    //implementation("dev.langchain4j:langchain4j:0.35.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.hivemq:hivemq-mqtt-client:1.3.3")
    implementation("org.testcontainers:testcontainers:1.20.3")

}

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.13.1")
    }
}

springBoot {
    mainClass.set("ai.ancf.lmos.wot.integration.AgentApplicationKt")
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
    systemProperty("otel.service.name", "chat-agent")
    //systemProperty("otel.javaagent.debug", "true")
}
