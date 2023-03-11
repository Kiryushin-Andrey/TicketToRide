import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
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

dependencies {
    implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))
    implementation(kotlinw("react"))
    implementation(kotlinw("react-dom"))
    implementation(kotlinw("extensions"))
    implementation(kotlinw("mui"))
    implementation(kotlinw("mui-icons"))
    implementation(kotlinw("emotion"))
}

kotlin {
    js(IR) {
        browser()
        useCommonJs()
        binaries.executable()
    }

    sourceSets {
        val main by getting {
            dependencies {
                implementation(project(":common"))
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
    val devJs by named<KotlinWebpack>("browserDevelopmentWebpack")
    val prodJs by named<KotlinWebpack>("browserProductionWebpack")

    val generatedJs by configurations.creating {
        isCanBeConsumed = true
        isCanBeResolved = false
        extendsFrom(configurations["implementation"], configurations["runtimeOnly"])
    }

    artifacts {
        add("generatedJs", devJs.outputFile) {
            builtBy(devJs)
        }
    }
}
