package ru.vladislavsumin.core.logger.common

/**
 * Основной класс логера.
 *
 * Мы вынужденны использовать именно класс для удобства конечного использования по нескольким причинам:
 * 1) Мы хотим что бы функции принимающие лямбды были inline функциями, так как это позволит избежать генерации
 *    анонимных классов при сборке в jvm/android или других накладных расходам при сборках под другие платформы.
 * 2) Мы не можем использовать inline функции в декларации интерфейса из-за ограничений языка.
 * 3) При декларации инлайн функций как функции расширения к интерфейсу их использование будет требовать явного импорта,
 *    что не удобно, особенно учитывая схожесть их имен с вариантом который принимает строку вместо лямбды. При таких
 *    коллизиях студия часто будет предлагать другой вариант при ошибочном использовании которого импорт добавлен не
 *    будет, что заставит пользователя позже вручную добавлять нужный импорт.
 * Таким образом отказ от интерфейса в пользу абстрактного класса кажется оптимальным решением.
 *
 * Второй набор костылей связан с желанием ограничить вызов функций [logInternal], так как они в целях повышения
 * производительности не должны повторно проверять [logLevel]. Однако мы не можем просто так сделать их protected,
 * так-как тогда к ним не будет доступа из inline функций (которые нам нужны по причинам озвученным выше). Хорошо что
 * разработчики языка уже придумали свои костыли в виде [PublishedApi], которые как раз предназначены для обхода этой
 * проблемы. Поэтому добавляем [accessLogInternal] как обертку над [logInternal] для возможности использовать эту
 * функцию из inline функций не делая ее публичной.
 */
@Suppress("TooManyFunctions") // Для логера допустимо большое количество функций так как они все типовые.
public abstract class Logger {
    /**
     * Возвращает текущий уровень логирования для данного логера.
     */
    public abstract val logLevel: LogLevel

    /**
     * Логирует **без** проверки уровня (это необходимо, что бы не проверять уровень логирования дважды).
     */
    protected abstract fun logInternal(level: LogLevel, msg: String)

    /**
     * Логирует **без** проверки уровня (это необходимо, что бы не проверять уровень логирования дважды).
     */
    protected abstract fun logInternal(level: LogLevel, throwable: Throwable, msg: String)

    @PublishedApi
    internal fun accessLogInternal(level: LogLevel, msg: String): Unit = logInternal(level, msg)

    @PublishedApi
    internal fun accessLogInternal(level: LogLevel, throwable: Throwable, msg: String): Unit =
        logInternal(level, throwable, msg)

    public fun log(level: LogLevel, msg: String) {
        if (logLevel.allowLog(logLevel)) {
            logInternal(level, msg)
        }
    }

    public fun log(level: LogLevel, throwable: Throwable, msg: String) {
        if (logLevel.allowLog(logLevel)) {
            logInternal(level, throwable, msg)
        }
    }

    public inline fun log(level: LogLevel, msg: () -> String) {
        if (logLevel.allowLog(level)) {
            accessLogInternal(level, msg())
        }
    }

    public inline fun log(level: LogLevel, throwable: Throwable, msg: () -> String) {
        if (logLevel.allowLog(level)) {
            accessLogInternal(level, throwable, msg())
        }
    }

    public fun t(throwable: Throwable, msg: String): Unit = log(LogLevel.TRACE, throwable, msg)
    public fun t(msg: String): Unit = log(LogLevel.TRACE, msg)
    public inline fun t(throwable: Throwable, msg: () -> String): Unit = log(LogLevel.TRACE, throwable, msg)
    public inline fun t(msg: () -> String): Unit = log(LogLevel.TRACE, msg)

    public fun d(throwable: Throwable, msg: String): Unit = log(LogLevel.DEBUG, throwable, msg)
    public fun d(msg: String): Unit = log(LogLevel.DEBUG, msg)
    public inline fun d(throwable: Throwable, msg: () -> String): Unit = log(LogLevel.DEBUG, throwable, msg)
    public inline fun d(msg: () -> String): Unit = log(LogLevel.DEBUG, msg)

    public fun i(throwable: Throwable, msg: String): Unit = log(LogLevel.INFO, throwable, msg)
    public fun i(msg: String): Unit = log(LogLevel.INFO, msg)
    public inline fun i(throwable: Throwable, msg: () -> String): Unit = log(LogLevel.INFO, throwable, msg)
    public inline fun i(msg: () -> String): Unit = log(LogLevel.INFO, msg)

    public fun w(throwable: Throwable, msg: String): Unit = log(LogLevel.WARN, throwable, msg)
    public fun w(msg: String): Unit = log(LogLevel.WARN, msg)
    public inline fun w(throwable: Throwable, msg: () -> String): Unit = log(LogLevel.WARN, throwable, msg)
    public inline fun w(msg: () -> String): Unit = log(LogLevel.WARN, msg)

    public fun e(throwable: Throwable, msg: String): Unit = log(LogLevel.ERROR, throwable, msg)
    public fun e(msg: String): Unit = log(LogLevel.ERROR, msg)
    public inline fun e(throwable: Throwable, msg: () -> String): Unit = log(LogLevel.ERROR, throwable, msg)
    public inline fun e(msg: () -> String): Unit = log(LogLevel.ERROR, msg)

    public fun f(throwable: Throwable, msg: String): Unit = log(LogLevel.FATAL, throwable, msg)
    public fun f(msg: String): Unit = log(LogLevel.FATAL, msg)
    public inline fun f(throwable: Throwable, msg: () -> String): Unit = log(LogLevel.FATAL, throwable, msg)
    public inline fun f(msg: () -> String): Unit = log(LogLevel.FATAL, msg)
}
