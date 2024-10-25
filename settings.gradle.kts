pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "kotlin-wot"
include("kotlin-wot")
include("kotlin-wot-spring-starter")
include("kotlin-wot-binding-http")