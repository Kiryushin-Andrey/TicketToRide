import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
}

group = "ticket-to-ride"
version = "1.0.0"

repositories {
    mavenLocal()    // for muirwik compiled locally with Kotlin 1.6.0
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.6.0")
}

val reactVersion = "17.0.2"

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

                implementation(npm("react", reactVersion))
                implementation(npm("react-dom", reactVersion))
                implementation(npm("react-is", reactVersion))
                implementation(npm("styled-components", "5.2.0"))
                implementation(npm("inline-style-prefixer", "6.0.0"))
                implementation(npm("pigeon-maps", "0.19.7"))
                implementation(npm("fscreen", "1.2.0"))
                implementation(npm("@material-ui/core", "4.11.0"))
                compileOnly(npm("raw-loader", "4.0.1"))

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.3-pre.268-kotlin-1.6.0")

                // compiled locally from https://github.com/Kiryushin-Andrey/muirwik/tree/kotlin-1.6.0 and published to local maven
                implementation("com.ccfraser.muirwik:muirwik-components:0.9.1-kotlin-1.6.0")
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
