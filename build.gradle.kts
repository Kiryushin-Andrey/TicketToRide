//import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
//import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
//import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
//import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
//import com.bmuschko.gradle.docker.tasks.image.Dockerfile

plugins {
    kotlin("multiplatform") version "1.5.0" apply false
    kotlin("plugin.serialization") version "1.5.0" apply false
//    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

val dockerImageForHeroku = "registry.heroku.com/ticketgame/web"

//tasks {
//    val shadowJar = named("server:shadowJar")
//
//    val createDockerfile = create<Dockerfile>("dockerFile") {
//        group = "docker"
//        from("openjdk:8-alpine")
//        copyFile(shadowJar.map {
//            val jarPath = it.outputs.files.singleFile.relativeTo(buildDir).invariantSeparatorsPath
//            Dockerfile.CopyFile(jarPath, "/")
//        })
//        workingDir("/")
//        exposePort(8080)
//        entryPoint(shadowJar.map { listOf("java", "-jar", it.outputs.files.singleFile.name) })
//    }
//
//    val buildDockerImage = create<DockerBuildImage>("dockerBuildImage") {
//        group = "docker"
//        dependsOn(shadowJar)
//        images.add(dockerImageForHeroku)
//        inputDir.set(buildDir)
//        dockerFile.set(createDockerfile.destFile)
//    }
//
//    val createDockerContainer = create<DockerCreateContainer>("dockerCreateContainer") {
//        group = "docker"
//        dependsOn(buildDockerImage)
//        this.imageId.set(dockerImageForHeroku)
//        hostConfig.portBindings.add("8080:8080")
//        hostConfig.autoRemove.set(true)
//    }
//
//    create<DockerStartContainer>("dockerStartContainer") {
//        group = "docker"
//        dependsOn(createDockerContainer)
//        containerId.set(createDockerContainer.containerId)
//    }
//
//    create<DockerPushImage>("dockerPushToHeroku") {
//        group = "docker"
//        dependsOn(buildDockerImage)
//        images.add(dockerImageForHeroku)
//        registryCredentials {
//            url.set("registry.heroku.com")
//        }
//    }
//

//    register("backupYarnLock") {
//        dependsOn(":kotlinNpmInstall")
//
//        doLast {
//            copy {
//                from("$rootDir/build/js/yarn.lock")
//                rename { "yarn.lock.bak" }
//                into(rootDir)
//            }
//        }
//
//        inputs.file("$rootDir/build/js/yarn.lock").withPropertyName("inputFile")
//        outputs.file("$rootDir/yarn.lock.bak").withPropertyName("outputFile")
//    }
//    val restoreYarnLock = register("restoreYarnLock") {
//        doLast {
//            copy {
//                from("$rootDir/yarn.lock.bak")
//                rename { "yarn.lock" }
//                into("$rootDir/build/js")
//            }
//        }
//
//        inputs.file("$rootDir/yarn.lock.bak").withPropertyName("inputFile")
//        outputs.file("$rootDir/build/js/yarn.lock").withPropertyName("outputFile")
//    }
//
//    named("kotlinNpmInstall").configure {
//        dependsOn(restoreYarnLock)
//    }
//
//    register("validateYarnLock") {
//        dependsOn(":kotlinNpmInstall")
//
//        doLast {
//            val expected = file("$rootDir/yarn.lock.bak").readText()
//            val actual = file("$rootDir/build/js/yarn.lock").readText()
//
//            if (expected != actual) {
//                throw AssertionError(
//                    "Generated yarn.lock differs from the one in the repository. " +
//                            "It can happen because someone has updated a dependency and haven't run `./gradlew :backupYarnLock --refresh-dependencies` " +
//                            "afterwards."
//                )
//            }
//        }
//
//        inputs.files("$rootDir/yarn.lock.bak", "$rootDir/build/js/yarn.lock").withPropertyName("inputFiles")
//    }
//}
