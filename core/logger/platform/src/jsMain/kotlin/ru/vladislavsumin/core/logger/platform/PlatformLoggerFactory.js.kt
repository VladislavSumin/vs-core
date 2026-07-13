package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.ExternalLoggerFactory

@Suppress("UNUSED_PARAMETER")
internal actual fun createPlatformLoggerFactory(logPath: LogPath, stdout: Boolean): ExternalLoggerFactory =
    ExternalLoggerFactory { tag ->
        JsExternalLogger(tag, stdout)
    }

// TODO написать нормальные js логи
private class JsExternalLogger(private val tag: String, private val stdout: Boolean) : ExternalLogger {
    override fun log(level: LogLevel, msg: String) {
        if (stdout) println("[$level] ($tag) $msg")
    }

    override fun log(level: LogLevel, throwable: Throwable, msg: String) {
        log(level, msg)
        if (stdout) throwable.printStackTrace()
    }
}
