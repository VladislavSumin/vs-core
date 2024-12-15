package ru.vladislavsumin.utils

import org.gradle.api.Project

/**
 * Защита от выполнения кода при генерации kotlin dsl при использовании composite builds содержащих convention плагины.
 */
inline fun Project.protectFromDslAccessors(block: () -> Unit) {
    if (!isDslAccessors) {
        block()
    }
}

val Project.isDslAccessors: Boolean get() = name == "gradle-kotlin-dsl-accessors"