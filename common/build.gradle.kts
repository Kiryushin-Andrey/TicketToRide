plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("dev.icerock.mobile.multiplatform-resources")
}

val serializationVersion = "1.5.0"
val kotestVersion = "5.5.5"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
    androidTarget()
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
                api(libs.moko.resources)
            }
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
        val jsMain by getting {
            dependsOn(commonMain)
        }
        val androidMain by getting {
            dependsOn(commonMain)
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

multiplatformResources {
    multiplatformResourcesPackage = "ticketToRide.common"
}

android {
    compileSdk = 34
    namespace = "org.akir.ticketToRide"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 28
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
