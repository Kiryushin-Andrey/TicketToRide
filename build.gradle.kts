import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.bmuschko.gradle.docker.tasks.image.*
import com.bmuschko.gradle.docker.tasks.container.*
import com.codingfeline.buildkonfig.gradle.*
import com.codingfeline.buildkonfig.compiler.*
import java.io.ByteArrayOutputStream

plugins {
    application
    kotlin("multiplatform") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.codingfeline.buildkonfig") version "0.7.0"
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
}

application {
    // TODO use mainClass after https://github.com/johnrengelman/shadow/issues/609 gets fixed
    mainClassName = "ticketToRide.ServerKt"
}

val ktor_version = "1.5.0"
val serialization_version = "1.0.1"
val kotest_version = "4.3.2"
val kotlin_wrappers_version = "1.0.0-pre.134-kotlin-1.4.21"
val react_version = "16.13.0"
val dockerImageForHeroku = "registry.heroku.com/ticketgame/web"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js {
        browser {
            webpackTask {
                saveEvaluatedConfigFile = true
            }
        }
        useCommonJs()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serialization_version")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.2")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains:kotlin-css:$kotlin_wrappers_version")
                implementation("org.jetbrains:kotlin-css-jvm:$kotlin_wrappers_version")
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-serialization:$ktor_version")
                implementation("io.ktor:ktor-html-builder:$ktor_version")
                implementation("io.ktor:ktor-websockets:$ktor_version")
                implementation("io.github.microutils:kotlin-logging:1.7.9")
                implementation("org.slf4j:slf4j-simple:1.7.29")
            }
            languageSettings.apply {
                useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:$kotest_version")
                implementation("io.kotest:kotest-assertions-core-jvm:$kotest_version")
                implementation("io.kotest:kotest-property:$kotest_version")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(npm("react", react_version))
                implementation(npm("react-dom", react_version))
                implementation(npm("react-is", react_version))
                implementation(npm("@material-ui/core", "4.9.8"))
                implementation(npm("styled-components", "5.2.0"))
                implementation(npm("inline-style-prefixer", "6.0.0"))
                implementation(npm("pigeon-maps", "0.17.0"))
                implementation(npm("fscreen", "1.2.0"))
                compileOnly(npm("raw-loader", "4.0.1"))

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
                implementation("org.jetbrains:kotlin-styled:5.2.0-pre.134-kotlin-1.4.21")
                implementation("com.ccfraser.muirwik:muirwik-components:0.6.3")
            }
        }
    }

    kotlin.sourceSets.all {
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    configure<BuildKonfigExtension> {
        fun getGitHash(): String {
            val versionFromEnv = System.getenv("SOURCE_VERSION")
            if (versionFromEnv != null)
                return versionFromEnv

            val stdout = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = stdout
            }
            return stdout.toString().trim()
        }

        packageName = "ticketToRide"
        defaultConfigs {
            buildConfigField(FieldSpec.Type.STRING, "version", getGitHash())
        }
    }

    val devJs = named<KotlinWebpack>("jsBrowserDevelopmentWebpack")
    val prodJs = named<KotlinWebpack>("jsBrowserProductionWebpack")

    val shadowJar = named<ShadowJar>("shadowJar") {
        manifest {
            attributes("Main-Class" to application.mainClass.get())
        }
        archiveFileName.set("ticket-to-ride.fat.jar")
        val jvmCompilation = kotlin.jvm().compilations["main"]
        configurations = mutableListOf(jvmCompilation.compileDependencyFiles as Configuration)
        from(jvmCompilation.output)
        from(prodJs)
    }

    val devJar = create<Jar>("developmentJar") {
        from(kotlin.jvm().compilations["main"].output)
        from(devJs)
    }

    create<JavaExec>("runDev") {
        group = "application"
        main = application.mainClass.get()
        classpath(configurations["jvmRuntimeClasspath"], devJar)
    }

    val createDockerfile = create<Dockerfile>("dockerFile") {
        group = "docker"
        from("openjdk:8-alpine")
        copyFile(shadowJar.map {
            val jarPath = it.outputs.files.singleFile.relativeTo(buildDir).invariantSeparatorsPath
            Dockerfile.CopyFile(jarPath, "/")
        })
        workingDir("/")
        exposePort(8080)
        entryPoint(shadowJar.map { listOf("java", "-jar", it.outputs.files.singleFile.name) })
    }

    val buildDockerImage = create<DockerBuildImage>("dockerBuildImage") {
        group = "docker"
        dependsOn(shadowJar)
        images.add(dockerImageForHeroku)
        inputDir.set(buildDir)
        dockerFile.set(createDockerfile.destFile)
    }

    val createDockerContainer = create<DockerCreateContainer>("dockerCreateContainer") {
        group = "docker"
        dependsOn(buildDockerImage)
        this.imageId.set(dockerImageForHeroku)
        hostConfig.portBindings.add("8080:8080")
        hostConfig.autoRemove.set(true)
    }

    create<DockerStartContainer>("dockerStartContainer") {
        group = "docker"
        dependsOn(createDockerContainer)
        containerId.set(createDockerContainer.containerId)
    }

    create<DockerPushImage>("dockerPushToHeroku") {
        group = "docker"
        dependsOn(buildDockerImage)
        images.add(dockerImageForHeroku)
        registryCredentials {
            url.set("registry.heroku.com")
        }
    }
}
