package ru.vladislavsumin.utils

import dev.detekt.gradle.Detekt
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

private object DetektConfigLoader

/**
 * Регистрирует в данном [Project] задачу которая будет запускать detekt для "внешнего" модуля,
 * обычно используется для подключения детекта к скриптам сборки.
 */
fun Project.registerExternalModuleDetektTask(
    taskName: String,
    moduleDir: File,
    dependsOnDetekt: Boolean = true,
): TaskProvider<Detekt> = tasks.register<Detekt>(taskName) {
    source = fileTree(moduleDir) {
        include("src/**/*")
        include("*.gradle.kts")
    }
}.also {
    if (dependsOnDetekt) {
        tasks.named("detekt").configure { dependsOn(it) }
    }
}

/**
 * Регистрирует таску [resolveDetektBaseConfig] в корневом проекте, которая копирует
 * базовый конфиг detekt из classpath (упакован в JAR build-scripts) в build-директорию.
 * Таска будет up-to-date благодаря [outputs.file] и [inputs.property].
 */
fun Project.registerDetektBaseConfigTask(): TaskProvider<DefaultTask> {
    val resourceUrl = requireNotNull(
        DetektConfigLoader::class.java.classLoader.getResource("config/analyze/detekt.yml"),
    ) {
        "detekt.yml not found in build-scripts classpath. " +
            "Ensure it exists at build-scripts/src/main/resources/config/analyze/detekt.yml"
    }

    return tasks.register<DefaultTask>("resolveDetektBaseConfig") {
        val outputFile = layout.buildDirectory.file("tmp/detekt-base-config.yml")
        outputs.file(outputFile)
        outputs.cacheIf { true }

        inputs.property("configContentHash") {
            resourceUrl.openStream().use { it.readBytes().contentHashCode() }
        }

        doLast {
            val dest = outputFile.get().asFile
            dest.parentFile.mkdirs()
            resourceUrl.openStream().use { input ->
                dest.outputStream().use { input.copyTo(it) }
            }
        }
    }
}
