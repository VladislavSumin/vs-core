package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.LoggerManager

/**
 * Инициализирует логер с помощью платформенного внешнего логера по умолчанию
 */
fun LoggerManager.initDefault(rootLogLevel: LogLevel = LogLevel.TRACE) {
    init(createPlatformLoggerFactory(), rootLogLevel)
}
