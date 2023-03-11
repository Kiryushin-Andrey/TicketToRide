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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")
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