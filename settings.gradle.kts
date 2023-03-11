pluginManagement {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
    }
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("js") version kotlinVersion
    }
}

rootProject.name = "ticket-to-ride"
include("server")
include("client")
include("common")
