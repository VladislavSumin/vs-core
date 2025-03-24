package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.ExternalLoggerFactory

internal actual fun createPlatformLoggerFactory(): ExternalLoggerFactory {
    return ExternalLoggerFactory { tag ->
        MacOsExternalLogger(tag)
    }
}

// TODO написать нормальные js логи
private class MacOsExternalLogger(
    private val tag: String,
) : ExternalLogger {
    override fun log(level: LogLevel, msg: String) {
        println("[$level] ($tag) $msg")
    }

    override fun log(level: LogLevel, throwable: Throwable, msg: String) {
        println("[$level] ($tag) $msg")
        throwable.printStackTrace()
    }
}
