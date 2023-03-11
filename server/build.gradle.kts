plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("ticketToRide.ServerKt")
}

val ktorVersion = "1.6.4"
val kotlinWrappersVersion = "1.0.0-pre.508"
val serializationVersion = "1.5.0"
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-css:$kotlinWrappersVersion")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-css-jvm:$kotlinWrappersVersion")
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
                implementation("io.github.microutils:kotlin-logging:1.7.9")
                implementation("org.slf4j:slf4j-simple:1.7.29")
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
