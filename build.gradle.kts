plugins {
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("org.cadixdev.licenser") version "0.6.1"
    id("com.jaredsburrows.license") version "0.9.8"
    `maven-publish`
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "maven-publish")
    apply(plugin = "org.cadixdev.licenser")
    apply(plugin = "com.jaredsburrows.license")

    group = "org.eclipse.thingweb"
    version = "0.1.3-SNAPSHOT"

    license {
        header(rootProject.file("LICENSE"))
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

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
        dependsOn("classes")
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(tasks.javadoc)
        dependsOn("javadoc")
    }

    artifacts {
        add("archives", tasks["sourcesJar"])
        add("archives", tasks["javadocJar"])
    }

    publishing {
        publications {
            create<MavenPublication>("mavenKotlin") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
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
        jvmToolchain(21)
    }

}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}
