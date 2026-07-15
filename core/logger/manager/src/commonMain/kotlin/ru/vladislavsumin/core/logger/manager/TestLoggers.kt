package ru.vladislavsumin.core.logger.manager

import ru.vladislavsumin.core.logger.common.LogLevel

public fun LoggerManager.initTest() {
    val current = factory
    if (current != null) {
        check(current === TestLoggerFactory) { "Logger already initialized with a non-test factory" }
        return
    }
    init(TestLoggerFactory)
}

private object TestLoggerFactory : ExternalLoggerFactory {
    override fun create(tag: String): ExternalLogger = NoOpExternalLogger
}

private object NoOpExternalLogger : ExternalLogger {
    override fun log(level: LogLevel, msg: String) = Unit
    override fun log(level: LogLevel, throwable: Throwable, msg: String) = Unit
}
