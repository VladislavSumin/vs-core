package ru.vladislavsumin.core.navigation.testData

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.common.Logger
import ru.vladislavsumin.core.logger.internal.LoggerFactory

/**
 * Инициализирует no-op логер для тестов навигации.
 *
 * Навигация активно использует [ru.vladislavsumin.core.navigation.NavigationLogger], который без инициализации
 * фабрики логеров бросает `IllegalStateException: Logger not initialized`. Установка [LoggerFactory] идемпотентна,
 * поэтому вызывать этот метод можно из каждого теста.
 */
fun initTestLogger() {
    LoggerFactory = { _, _ -> NoOpLogger }
}

private object NoOpLogger : Logger() {
    override val logLevel: LogLevel = LogLevel.FATAL
    override fun logInternal(level: LogLevel, msg: String) = Unit
    override fun logInternal(level: LogLevel, throwable: Throwable, msg: String) = Unit
}
