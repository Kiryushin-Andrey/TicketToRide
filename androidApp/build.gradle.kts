plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

val serverHost: String by project

android {
    compileSdk = 34
    namespace = "org.akir.ticketToRide"

    @Suppress("UnstableApiUsage")
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.akir.ticketToRide"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        compileSdkPreview = "UpsideDownCake"

        manifestPlaceholders["SERVER_HOST"] = serverHost
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
    androidTarget()
}

dependencies {
    implementation(libs.ktor.client.okhttp)
    implementation(project(":client-shared"))
    implementation(project(":compose-shared"))
    implementation("androidx.compose.ui:ui-tooling-preview-android:1.6.8")
}
