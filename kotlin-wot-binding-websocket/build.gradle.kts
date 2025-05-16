dependencies {
    implementation(platform("io.ktor:ktor-bom:3.0.3"))

    api(project(":kotlin-wot"))
    api(project(":kotlin-wot-lmos-protocol"))
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-client-websocket:1.1.4")
    implementation("io.ktor:ktor-server-content-negotiation")

    // Tracing
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:2.17.0-alpha-SNAPSHOT")

    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-auth")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(project(":kotlin-wot-binding-http"))
    testImplementation("ch.qos.logback:logback-classic:1.5.12")
    testImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.0")
}
