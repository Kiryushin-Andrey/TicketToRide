import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.bmuschko.gradle.docker.tasks.image.*
import com.bmuschko.gradle.docker.tasks.container.*
import com.codingfeline.buildkonfig.compiler.*
import java.io.ByteArrayOutputStream

plugins {
    application
    kotlin("multiplatform") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.codingfeline.buildkonfig") version "0.7.0"
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

repositories {
    mavenLocal()    // for muirwik compiled locally with IR and Kotlin 1.5
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
}

application {
    mainClass.set("ticketToRide.ServerKt")
}

val ktorVersion = "1.5.4"
val serializationVersion = "1.2.0"
val kotestVersion = "4.5.0"
val kotlinWrappersVersion = "1.0.0-pre.154-kotlin-1.5.0"
val reactVersion = "16.13.0"
val dockerImageForHeroku = "registry.heroku.com/ticketgame/web"

kotlin {
    jvm()
    js(IR) {
        browser()
        useCommonJs()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.4")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", "1.5.0"))
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
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
                implementation("io.kotest:kotest-property:$kotestVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(npm("react", reactVersion))
                implementation(npm("react-dom", reactVersion))
                implementation(npm("react-is", reactVersion))
                implementation(npm("styled-components", "5.2.0"))
                implementation(npm("inline-style-prefixer", "6.0.0"))
                implementation(npm("pigeon-maps", "0.17.0"))
                implementation(npm("fscreen", "1.2.0"))
                compileOnly(npm("raw-loader", "4.0.1"))

                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
                implementation("org.jetbrains:kotlin-styled:5.2.3-pre.154-kotlin-1.5.0")

                // compiled locally from https://github.com/Kiryushin-Andrey/muirwik/tree/IR-Compiler and published to local maven
                implementation("com.ccfraser.muirwik:muirwik-components:0.6.7-kotlin-IR-1.5")
            }
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.js.ExperimentalJsExport")
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

buildkonfig {
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

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
    withType<KotlinWebpack> {
        saveEvaluatedConfigFile = false
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
