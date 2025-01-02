package ru.vladislavsumin.convention.analyze

/**
 * Настройка detekt плагина по умолчанию для всех модулей.
 */

check(project === rootProject) { "This convention may be applied only to root project" }

allprojects {
    apply {
        plugin("ru.vladislavsumin.convention.analyze.detekt")
    }
}
