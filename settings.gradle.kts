pluginManagement {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.codingfeline.buildkonfig") {
                useModule("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "ticket-to-ride"
include("server")
include("client")
include("common")
