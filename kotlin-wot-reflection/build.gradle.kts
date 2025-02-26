dependencies {
    api(project(":kotlin-wot"))
    implementation("org.slf4j:slf4j-api:2.0.16")
    //implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation(project(":kotlin-wot-binding-http"))
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
}