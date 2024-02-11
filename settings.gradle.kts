pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val agpVersion = extra["agp.version"] as String
        val composeVersion = extra["compose.version"] as String
        val mokoVersion = extra["moko.version"] as String

        kotlin("jvm") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("android") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.android.base") version agpVersion
        id("com.android.application") version agpVersion
        id("com.android.library") version agpVersion
        id("org.jetbrains.compose") version composeVersion
        id("com.codingfeline.buildkonfig") version "0.14.0"
        id("dev.icerock.mobile.multiplatform-resources") version mokoVersion
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinx.serialization", "1.6.0")
            version("kotlinx.coroutines", "1.7.3")
            version("ktor", "2.3.3")
            version("moko", extra["moko.version"] as String)

            library("kotlinx.coroutines.core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlinx.coroutines")
            library("kotlinx.serialization.core", "org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef("kotlinx.serialization")
            library("kotlinx.serialization.json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlinx.serialization")
            library("kotlinx.serialization.protobuf", "org.jetbrains.kotlinx", "kotlinx-serialization-protobuf").versionRef("kotlinx.serialization")

            library("ktor.server.core", "io.ktor", "ktor-server-core").versionRef("ktor")
            library("ktor.server.html-builder", "io.ktor", "ktor-server-html-builder").versionRef("ktor")
            library("ktor.server.netty", "io.ktor", "ktor-server-netty").versionRef("ktor")
            library("ktor.server.caching-headers", "io.ktor", "ktor-server-caching-headers").versionRef("ktor")
            library("ktor.server.compression", "io.ktor", "ktor-server-compression").versionRef("ktor")
            library("ktor.server.content-negotiation", "io.ktor", "ktor-server-content-negotiation").versionRef("ktor")
            library("ktor.server.websockets", "io.ktor", "ktor-server-websockets").versionRef("ktor")

            library("ktor.client.java", "io.ktor", "ktor-client-java").versionRef("ktor")
            library("ktor.client.okhttp", "io.ktor", "ktor-client-okhttp").versionRef("ktor")
            library("ktor.client.websockets", "io.ktor", "ktor-client-websockets").versionRef("ktor")

            library("ktor.serialization.kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")

            library("oshai.kotlin.logging", "io.github.oshai", "kotlin-logging-jvm").version("6.0.2")
            library("slf4j", "org.slf4j", "slf4j-simple").version("2.0.11")

            library("moko.resources", "dev.icerock.moko", "resources").versionRef("moko")
            library("moko.resources.compose", "dev.icerock.moko", "resources-compose").versionRef("moko")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "ticket-to-ride"
include("server")
include("client")
include("client-shared")
include("common")
include("compose-shared")
include("desktopApp")
include("androidApp")
