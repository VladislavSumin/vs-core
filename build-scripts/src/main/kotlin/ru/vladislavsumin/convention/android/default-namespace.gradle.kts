package ru.vladislavsumin.convention.android

import ru.vladislavsumin.utils.android
import ru.vladislavsumin.utils.fullName

/**
 * Устанавливает базовый namespace для android модулей вида ru.vladislavsumin.***, где *** заменяются на полное имя проекта.
 */

android {
    namespace = "ru.vladislavsumin.${project.fullName()}"
}
