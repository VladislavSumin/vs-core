package ru.vladislavsumin.core.logger.manager

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.common.Logger

internal class LoggerImpl(
    private val logger: ExternalLogger,
    override val logLevel: LogLevel,
) : Logger() {
    override fun logInternal(level: LogLevel, msg: String) {
        logger.log(level, msg)
    }

    override fun logInternal(level: LogLevel, throwable: Throwable, msg: String) {
        logger.log(level, throwable, msg)
    }
}
