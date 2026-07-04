package ru.vladislavsumin.convention.analyze

import ru.vladislavsumin.utils.registerDetektBaseConfigTask

/**
 * Настройка detekt плагина по умолчанию для всех модулей.
 */

check(project === rootProject) { "This convention may be applied only to root project" }

registerDetektBaseConfigTask()

allprojects {
    apply {
        plugin("ru.vladislavsumin.convention.analyze.detekt")
    }
}
