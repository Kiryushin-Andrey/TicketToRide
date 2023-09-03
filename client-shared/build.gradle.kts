import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.codingfeline.buildkonfig")
}

kotlin {
    jvmToolchain(17)
    jvm()
    js(IR) {
        browser()
        useCommonJs()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}

val serverHost: String by project

buildkonfig {
    packageName = "ticketToRide"
    exposeObjectWithName = "BuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "SERVER_HOST", serverHost)
    }
}
