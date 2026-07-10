package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.LoggerManager

/**
 * Инициализирует логер с помощью платформенного внешнего логера по умолчанию.
 * @param logPath путь к директории с логами.
 */
public fun LoggerManager.initDefault(logPath: LogPath = LogPath.WorkDir, rootLogLevel: LogLevel = LogLevel.TRACE) {
    init(createPlatformLoggerFactory(logPath), rootLogLevel)
}
