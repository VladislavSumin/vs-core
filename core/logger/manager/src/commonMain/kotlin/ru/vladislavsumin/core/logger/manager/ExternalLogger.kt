package ru.vladislavsumin.core.logger.manager

import ru.vladislavsumin.core.logger.common.LogLevel

/**
 * Внешний интерфейс для использования внешнего логера.
 */
public interface ExternalLogger {
    public fun log(level: LogLevel, msg: String)
    public fun log(level: LogLevel, throwable: Throwable, msg: String)
}

public fun interface ExternalLoggerFactory {
    public fun create(tag: String): ExternalLogger
}
