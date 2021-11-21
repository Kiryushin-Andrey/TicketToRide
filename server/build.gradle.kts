import java.io.ByteArrayOutputStream
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.gmazzo.buildconfig") version "3.0.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
}

application {
    mainClass.set("ticketToRide.ServerKt")
}

val ktorVersion = "1.5.4"
val kotlinWrappersVersion = "1.0.0-pre.154-kotlin-1.5.0"
val serializationVersion = "1.2.0"
val kotestVersion = "4.5.0"

val dockerImageForHeroku = "registry.heroku.com/ticketgame/web"

kotlin {
    sourceSets {
        val main by getting {
            dependencies {
                runtimeOnly(project(":client", "generatedJs")) {
                    isTransitive = false
                }
                implementation(project(":common"))
                implementation(kotlin("reflect", "1.5.0"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationVersion")
                implementation("org.jetbrains:kotlin-css:$kotlinWrappersVersion")
                implementation("org.jetbrains:kotlin-css-jvm:$kotlinWrappersVersion")
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-serialization:$ktorVersion")
                implementation("io.ktor:ktor-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-websockets:$ktorVersion")
                implementation("io.github.microutils:kotlin-logging:1.7.9")
                implementation("org.slf4j:slf4j-simple:1.7.29")
            }
            languageSettings.apply {
                useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        val test by getting {
            dependencies {
                implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    val shadowJar = named("shadowJar")

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

buildConfig {
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

    packageName("ticketToRide")
    buildConfigField("String", "version", '"' + getGitHash() + '"')
}
