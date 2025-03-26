package ru.vladislavsumin.convention.android

import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.android
import ru.vladislavsumin.utils.fullName
import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Устанавливает базовый namespace для android модулей вида ru.vladislavsumin.***,
 * где *** заменяются на полное имя проекта.
 */

protectFromDslAccessors {
    android {
        namespace = "${project.projectConfiguration.basePackage}.${project.fullName().replace("-", "_")}"
    }
}
