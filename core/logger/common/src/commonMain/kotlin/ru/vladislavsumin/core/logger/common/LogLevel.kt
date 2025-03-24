package ru.vladislavsumin.core.logger.common

/**
 * Перечисление уровней логирования.
 */
@Suppress("MagicNumber") // В данном случае смысл чисел понятен без вынесения в константы
public enum class LogLevel(private val rawLevel: Int) {
    TRACE(6),
    DEBUG(5),
    INFO(4),
    WARN(3),
    ERROR(2),
    FATAL(1),
    NONE(0),
    ;

    /**
     * Проверяет допустимо ли на этом уровне логирования логировать [logLevel]
     */
    @PublishedApi
    internal fun allowLog(logLevel: LogLevel): Boolean {
        return this.rawLevel >= logLevel.rawLevel
    }

    /**
     * Объединяет два [LogLevel] возвращая уровень с меньшим уровнем (более узкий)
     */
    public infix fun merge(logLevel: LogLevel): LogLevel {
        return if (this.rawLevel <= logLevel.rawLevel) this else logLevel
    }
}
