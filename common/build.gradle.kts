plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

val serializationVersion = "1.5.0"
val kotestVersion = "5.5.5"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        browser()
        useCommonJs()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.protobuf)
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-property:$kotestVersion")
            }
        }
    }

    kotlin.sourceSets.all {
        languageSettings.apply {
            optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}