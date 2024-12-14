package ru.vladislavsumin.convention.android

import ru.vladislavsumin.utils.android
import ru.vladislavsumin.utils.pathSequence

/**
 * Устанавливает базовый namespace для android модулей вида ru.vladislavsumin.***, где *** заменяются на полное имя проекта.
 */

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

android {
    namespace = "ru.vladislavsumin.${project.fullName()}"
}
