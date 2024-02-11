buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        val mokoVersion = project.property("moko.version") as String
        classpath("dev.icerock.moko:resources-generator:$mokoVersion")
    }
}

plugins {
    kotlin("jvm") apply false
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
    id("com.codingfeline.buildkonfig") apply false
    id("dev.icerock.mobile.multiplatform-resources") apply false
    kotlin("plugin.serialization") apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
