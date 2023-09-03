plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("ticketToRide.ServerKt")
}

val kotlinWrappersVersion = "1.0.0-pre.508"
val kotestVersion = "5.5.5"

val dockerImageName = "andreykir/ticket-to-ride"

kotlin {
    jvmToolchain(17)
    sourceSets {
        val main by getting {
            dependencies {
                runtimeOnly(project(":client", "generatedJs")) {
                    isTransitive = false
                }
                implementation(project(":common"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.protobuf)

                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.html.builder)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.caching.headers)
                implementation(libs.ktor.server.compression)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation("org.jetbrains.kotlin-wrappers:kotlin-css:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-css-jvm:$kotlinWrappersVersion")

                implementation(libs.oshai.kotlin.logging)
                implementation(libs.slf4j)
            }

            languageSettings.apply {
                optIn("kotlinx.coroutines.FlowPreview")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        val test by getting {
            dependencies {
                implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Copy>("processResources") {
    from("../maps") {
        include("**/*.map")
        into("maps")
    }
}
