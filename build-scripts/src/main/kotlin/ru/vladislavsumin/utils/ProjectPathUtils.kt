package ru.vladislavsumin.utils

import org.gradle.api.Project

/**
 * Возвращает последовательность проектов от текущего включительно до корня включительно.
 */
fun Project.pathSequence(): Sequence<Project> {
    var project: Project = this
    return sequence {
        while (true) {
            yield(project)
            project = project.parent ?: break
        }
    }
}

/**
 * Возвращает полное имя проекта, используя "." как разделитель
 */
fun Project.fullName(): String = pathSequence()
    .asIterable()
    .reversed()
    .drop(1) // отбрасываем root project
    .joinToString(separator = ".") {
        it.name.replace("-", "_")
    }
