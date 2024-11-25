dependencies {
    api(project(":kotlin-wot"))
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation(project(":kotlin-wot-binding-http"))
}