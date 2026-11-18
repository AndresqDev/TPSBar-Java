import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.security.MessageDigest

plugins {
    kotlin("multiplatform") version "2.4.0"
    id("org.pumpkinmc.plugin") version "0.1.2"
}

group = "com.ping.tpsbar"
version = "0.1.0"

val targetRoot = layout.projectDirectory.dir("target")
val targetKotlin = targetRoot.dir("kotlin")
val targetTraces = targetRoot.dir("traces")
val targetWasm = targetRoot.dir("wasm")
val wasmArtifactName = providers.gradleProperty("pumpkinWasmName").orElse("tpsbar-java")
val pluginClassName = "com.ping.tpsbar.TpsBarPlugin"
val stubJar = layout.projectDirectory.file("libs/pumpkin-stub.jar")
val translatorJar = layout.projectDirectory.file("libs/j2kt-pumpkin.jar")

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        named("wasmWasiMain") {
            kotlin.srcDir("compat")
            kotlin.srcDir(targetKotlin)
        }
    }
}

pumpkin {
    apiVersion.set("0.1.2")
    pluginClass.set(pluginClassName)
}

val translateJava = tasks.register<JavaExec>("translateJava") {
    group = "build"
    description = "Translate the Java plugin sources into Kotlin/Wasm sources."
    dependsOn(":java-plugin:compileJava")
    inputs.dir("java-plugin/src/main/java")
    inputs.file(stubJar)
    inputs.file(translatorJar)
    outputs.dir(targetKotlin)

    classpath = files(translatorJar)
    mainClass.set("dev.j2kt.Main")
    args(
        "--target", "pumpkin",
        "--no-header",
        "--out", targetKotlin.asFile.absolutePath,
        "--classpath", stubJar.asFile.absolutePath,
        "--report", targetTraces.file("translation-report.md").asFile.absolutePath,
        "java-plugin/src/main/java",
    )

    doFirst {
        targetKotlin.asFile.deleteRecursively()
        targetKotlin.asFile.mkdirs()
        targetTraces.asFile.mkdirs()
        logger.lifecycle("[2/4] Translating Java sources to Kotlin under ${targetKotlin.asFile.relativeTo(projectDir)}")
    }
    doLast {
        val files = targetKotlin.asFile.walkTopDown().filter { it.isFile }.count()
        logger.lifecycle("[2/4] Translated $files Kotlin file(s)")
    }
}

tasks.named("compileKotlinWasmWasi") {
    dependsOn(translateJava)
    doFirst {
        logger.lifecycle("[3/4] Compiling generated Kotlin into a WebAssembly component")
    }
}

tasks.register("buildWasm") {
    group = "build"
    description = "Compile Java, translate it to Kotlin, and package the Pumpkin wasm component."
    dependsOn(":java-plugin:compileJava", translateJava, "assemblePluginRelease")

    doLast {
        targetWasm.asFile.mkdirs()
        targetWasm.asFile.listFiles { file -> file.extension == "wasm" }?.forEach { it.delete() }
        val source = layout.buildDirectory.file("${project.name}.wasm").get().asFile
        check(source.isFile) {
            "Expected wasm component at ${source.absolutePath}. Check assemblePluginRelease output."
        }
        val destination = targetWasm.file("${wasmArtifactName.get()}.wasm").asFile
        source.copyTo(destination, overwrite = true)

        targetTraces.asFile.mkdirs()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(destination.readBytes())
            .joinToString("") { "%02x".format(it) }
        val summary = buildString {
            appendLine("Pumpkin Java to Kotlin to Wasm build")
            appendLine("API version: 0.1.2")
            appendLine("Wasm name: ${wasmArtifactName.get()}")
            appendLine("Plugin class: $pluginClassName")
            appendLine("Wasm: ${destination.relativeTo(projectDir)}")
            appendLine("Size: ${destination.length()} bytes")
            appendLine("SHA-256: $digest")
            appendLine("Kotlin sources: ${targetKotlin.asFile.relativeTo(projectDir)}")
            appendLine("Java classes: ${targetRoot.dir("java/classes").asFile.relativeTo(projectDir)}")
            appendLine("Translation report: ${targetTraces.file("translation-report.md").asFile.relativeTo(projectDir)}")
        }
        targetTraces.file("build-summary.txt").asFile.writeText(summary)

        val javaSources = fileTree("java-plugin/src/main/java") {
            include("**/*.java")
        }.files.map { it.relativeTo(projectDir).invariantSeparatorsPath }.sorted()
        val kotlinSources = targetKotlin.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.relativeTo(projectDir).invariantSeparatorsPath }
            .sorted()
            .toList()
        targetTraces.file("java-sources.txt").asFile.writeText(javaSources.joinToString("\n") + "\n")
        targetTraces.file("kotlin-sources.txt").asFile.writeText(kotlinSources.joinToString("\n") + "\n")

        logger.lifecycle("[4/4] Wrote ${destination.relativeTo(projectDir)} (${destination.length()} bytes)")
        logger.lifecycle("[4/4] Trace summary: ${targetTraces.file("build-summary.txt").asFile.relativeTo(projectDir)}")
    }
}

tasks.register("cleanBuildWasm") {
    group = "build"
    description = "Clean previous outputs and build the complete Pumpkin wasm plugin."
    dependsOn("clean", "buildWasm")
}

tasks.named("build") {
    setDependsOn(listOf("cleanBuildWasm"))
}

val rootClean = tasks.named("clean")
tasks.configureEach {
    if (!name.startsWith("clean")) {
        mustRunAfter(rootClean)
    }
}
subprojects {
    tasks.configureEach {
        if (!name.startsWith("clean")) {
            mustRunAfter(rootProject.tasks.named("clean"))
        }
    }
}

tasks.named("clean") {
    delete(targetRoot)
    dependsOn(":java-plugin:clean")
}
