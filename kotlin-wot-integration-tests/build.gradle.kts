dependencies {
    api(project(":kotlin-wot"))
    api(project(":kotlin-wot-binding-http"))
    api("ch.qos.logback:logback-classic:1.5.12")
    implementation("ai.ancf.lmos:arc-agents:0.98.0")
    implementation("ai.ancf.lmos:arc-langchain4j-client:0.98.0")
}