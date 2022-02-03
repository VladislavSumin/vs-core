package ru.vs.build_script.utils

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

fun TaskContainer.namedOrNull(
    taskName: String
): TaskProvider<Task>? {
    return try {
        named(taskName)
    } catch (_: Exception) {
        null
    }
}
