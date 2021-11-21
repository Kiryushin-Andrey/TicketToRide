import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js")
}

group = "ticket-to-ride"
version = "1.0.0"

repositories {
    mavenLocal()    // for muirwik compiled locally with IR and Kotlin 1.5
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

val reactVersion = "16.13.0"

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
                implementation(npm("pigeon-maps", "0.17.0"))
                implementation(npm("fscreen", "1.2.0"))
                implementation(npm("@material-ui/core", "4.11.0"))
                compileOnly(npm("raw-loader", "4.0.1"))

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
                implementation("org.jetbrains:kotlin-styled:5.2.3-pre.154-kotlin-1.5.0")

                // compiled locally from https://github.com/Kiryushin-Andrey/muirwik/tree/IR-Compiler and published to local maven
                implementation("com.ccfraser.muirwik:muirwik-components:0.6.7-kotlin-IR-1.5")
            }
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.js.ExperimentalJsExport")
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
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
        add("generatedJs", prodJs.outputFile) {
            builtBy(prodJs)
        }
    }
}
