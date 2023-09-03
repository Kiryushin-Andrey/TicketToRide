import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)
    jvm {}
    sourceSets {
        val jvmMain by getting  {
            dependencies {
                implementation(compose.material3)
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.java)
                implementation(libs.oshai.kotlin.logging)
                implementation(libs.slf4j)
                implementation(project(":compose-shared"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "org.akir.ticketToRide"
            packageVersion = "1.0.0"
            macOS {
                iconFile.set(project.file("favicon.ico"))
            }
            windows {
                iconFile.set(project.file("favicon.ico"))
            }
            linux {
                iconFile.set(project.file("favicon.ico"))
            }
        }
    }
}
