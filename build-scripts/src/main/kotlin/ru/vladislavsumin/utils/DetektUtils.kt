package ru.vladislavsumin.utils

import dev.detekt.gradle.Detekt
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

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
