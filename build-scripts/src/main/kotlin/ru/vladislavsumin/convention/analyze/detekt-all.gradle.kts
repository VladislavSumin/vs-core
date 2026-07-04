package ru.vladislavsumin.convention.analyze

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.registerDetektBaseConfigTask
import ru.vladislavsumin.utils.registerDetektCustomConfigTask

/**
 * Настройка detekt плагина по умолчанию для всех модулей.
 */

check(project === rootProject) { "This convention may be applied only to root project" }

registerDetektBaseConfigTask()
registerDetektCustomConfigTask(projectConfiguration.basePackage)

allprojects {
    apply {
        plugin("ru.vladislavsumin.convention.analyze.detekt")
    }
}
