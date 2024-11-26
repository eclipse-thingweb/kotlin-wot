plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("org.cadixdev.licenser") version "0.6.1"
}



subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "org.cadixdev.licenser")

    group = "ai.ancf.lmos"
    version = "1.0-SNAPSHOT"

    license {
        include("**/*.java")
        include("**/*.kt")
        include("**/*.yaml")
        exclude("**/*.properties")
    }

    dependencies {
        testImplementation(kotlin("test"))
        testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test")
        testImplementation("io.mockk:mockk:1.13.13")
    }

    tasks.test {
        useJUnitPlatform()
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

