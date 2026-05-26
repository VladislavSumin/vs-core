package ru.vladislavsumin.convention.analyze

/**
 * Настройка kover плагина по умолчанию. Должна подключаться ко всем модулям, в которых нужен анализ покрытия тестами.
 */

plugins {
    id("org.jetbrains.kotlinx.kover")
}
