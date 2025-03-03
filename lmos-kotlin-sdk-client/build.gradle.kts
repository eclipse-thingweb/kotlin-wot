dependencies {
    implementation(project(":kotlin-wot"))
    implementation(project(":kotlin-wot-binding-http"))
    implementation(project(":kotlin-wot-binding-websocket"))
    api(project(":lmos-kotlin-sdk-base"))
    implementation("org.slf4j:slf4j-api:2.0.16")
}