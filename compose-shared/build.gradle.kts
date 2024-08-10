plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
    kotlin("plugin.serialization")
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(libs.oshai.kotlin.logging)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization.json)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
                implementation(project(":common"))
                implementation(project(":client-shared"))
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                api("androidx.activity:activity-compose:1.8.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")

                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

                implementation("androidx.compose.ui:ui-tooling:1.6.3")
                implementation("androidx.compose.ui:ui-tooling-preview:1.6.3")
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(compose.desktop.common)
            }
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "ticketToRide"
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
dependencies {
    implementation("androidx.compose.material3:material3-android:1.2.1")
}
