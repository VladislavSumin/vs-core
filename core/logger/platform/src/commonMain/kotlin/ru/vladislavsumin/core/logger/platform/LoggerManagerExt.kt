package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.LoggerManager

/**
 * Инициализирует логер с помощью платформенного внешнего логера по умолчанию.
 * @param logPath путь к директории с логами.
 * @param stdout выводить ли логи в стандартный вывод (stdout / println). По умолчанию true.
 *   Установите false для headless-режимов, где stdout используется для машинного вывода (например, JSON).
 */
public fun LoggerManager.initDefault(
    logPath: LogPath = LogPath.WorkDir,
    rootLogLevel: LogLevel = LogLevel.TRACE,
    stdout: Boolean = true,
) {
    init(createPlatformLoggerFactory(logPath, stdout), rootLogLevel)
}
