package ru.vladislavsumin.core.logger.manager

import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.internal.LoggerFactory

/**
 * Класс для настройки логера
 */
public object LoggerManager {
    internal var factory: ExternalLoggerFactory? = null
        private set

    /**
     * Инициализирует логер.
     * @param externalLoggerFactory реализация фабрики внешних логеров в который будет переданы все логи прошедшие
     * фильтрацию.
     */
    public fun init(externalLoggerFactory: ExternalLoggerFactory, rootLogLevel: LogLevel = LogLevel.TRACE) {
        check(factory == null) { "Logger already initialized" }
        factory = externalLoggerFactory
        LoggerFactory = { tag, logLevel ->
            LoggerImpl(
                logger = externalLoggerFactory.create(tag),
                logLevel = rootLogLevel merge logLevel,
            )
        }
    }

    /**
     * Завершает работу логера. На JVM вызывает [org.apache.logging.log4j.LogManager.shutdown],
     * сбрасывая буферы асинхронных аппендеров. На остальных платформах — no-op.
     */
    public fun shutdown() {
        factory?.shutdown()
    }
}
