dependencies {
    api(project(":kotlin-wot"))
    api(project(":kotlin-wot-binding-http"))
    api(project(":kotlin-wot-binding-mqtt"))
    api(project(":kotlin-wot-reflection"))
    api("ch.qos.logback:logback-classic:1.5.12")
    implementation("ai.ancf.lmos:arc-agents:0.98.0")
    implementation("ai.ancf.lmos:arc-langchain4j-client:0.111.0")

    implementation("dev.langchain4j:langchain4j-azure-open-ai:0.35.0")
    implementation("dev.langchain4j:langchain4j:0.35.0")
    testImplementation("com.hivemq:hivemq-mqtt-client:1.3.3")
    implementation("org.testcontainers:testcontainers:1.20.3")
}