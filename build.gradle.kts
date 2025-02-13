plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    `maven-publish`
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "maven-publish")

    group = "ai.ancf.lmos"
    version = "1.0-SNAPSHOT"

    dependencies {
        testImplementation(kotlin("test"))
        testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test")
        testImplementation("io.mockk:mockk:1.13.13")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenKotlin") {
                from(components["java"])
                artifactId = project.name
            }
        }
        repositories {
            mavenLocal()
        }
    }


    tasks.test {
        useJUnitPlatform()

        // Configure proxy only if the required properties are set
        val proxyHost = System.getProperty("http.proxyHost")
        val proxyPort = System.getProperty("http.proxyPort")

        if (!proxyHost.isNullOrEmpty() && !proxyPort.isNullOrEmpty()) {
            systemProperty("http.proxyHost", proxyHost)
            systemProperty("http.proxyPort", proxyPort)
        }
    }

    kotlin {
        jvmToolchain(17)
    }

}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
