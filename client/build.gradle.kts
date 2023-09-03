import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("multiplatform")
}

group = "ticket-to-ride"
version = "1.0.0"

repositories {
    mavenCentral()
}

val reactVersion = "17.0.2"
val kotlinWrappersVersion = "1.0.0-pre.508"

fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

kotlin {
    js(IR) {
        browser()
        useCommonJs()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(project(":client-shared"))

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.websockets)

                implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))
                implementation(kotlinw("react"))
                implementation(kotlinw("react-dom"))
                implementation(kotlinw("extensions"))
                implementation(kotlinw("mui"))
                implementation(kotlinw("mui-icons"))
                implementation(kotlinw("emotion"))

                implementation(npm("pigeon-maps", "0.19.7"))
                implementation(npm("fscreen", "1.2.0"))
                implementation(npm("@hookstate/core", "4.0.0"))
                compileOnly(npm("raw-loader", "4.0.1"))
            }
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
    }
}

tasks {
    val devJs by named<KotlinWebpack>("jsBrowserDevelopmentWebpack")
    val prodJs by named<KotlinWebpack>("jsBrowserProductionWebpack")

    val jsProductionExecutableCompileSync by named("jsProductionExecutableCompileSync") {
        dependsOn(devJs)
    }

    val generatedJs by configurations.creating {
        isCanBeConsumed = true
        isCanBeResolved = false
        extendsFrom(configurations["jsMainImplementation"], configurations["jsMainRuntimeOnly"])
    }

    artifacts {
        add("generatedJs", devJs.mainOutputFile) {
            builtBy(devJs)
        }
    }
}
