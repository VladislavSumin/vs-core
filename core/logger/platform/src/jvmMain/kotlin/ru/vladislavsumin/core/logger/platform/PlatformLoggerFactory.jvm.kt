package ru.vladislavsumin.core.logger.platform

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.ExternalLoggerFactory

internal actual fun createPlatformLoggerFactory(): ExternalLoggerFactory = object : ExternalLoggerFactory {
    override fun create(tag: String): ExternalLogger = Log4jExternalLogger(LogManager.getLogger(tag))
    override fun shutdown() {
        LogManager.shutdown()
    }
}

private class Log4jExternalLogger(private val logger: Logger) : ExternalLogger {
    override fun log(level: LogLevel, msg: String) {
        logger.log(level.toLevel(), msg)
    }

    override fun log(level: LogLevel, throwable: Throwable, msg: String) {
        logger.log(level.toLevel(), msg, throwable)
    }

    private fun LogLevel.toLevel(): Level = when (this) {
        LogLevel.TRACE -> Level.TRACE
        LogLevel.DEBUG -> Level.DEBUG
        LogLevel.INFO -> Level.INFO
        LogLevel.WARN -> Level.WARN
        LogLevel.ERROR -> Level.ERROR
        LogLevel.FATAL -> Level.FATAL
        LogLevel.NONE -> Level.OFF
    }
}
