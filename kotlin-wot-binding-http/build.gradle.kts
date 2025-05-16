dependencies {
    implementation(platform("io.ktor:ktor-bom:3.0.3"))
    api(project(":kotlin-wot"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-auth")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-server-auto-head-response")

    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:2.17.0-alpha-SNAPSHOT")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("ch.qos.logback:logback-classic:1.5.12")
    testImplementation("com.marcinziolo:kotlin-wiremock:2.1.1")
}
