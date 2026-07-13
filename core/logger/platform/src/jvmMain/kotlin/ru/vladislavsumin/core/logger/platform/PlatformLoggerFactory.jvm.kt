package ru.vladislavsumin.core.logger.platform

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.ExternalLoggerFactory
import java.io.File

internal actual fun createPlatformLoggerFactory(logPath: LogPath, stdout: Boolean): ExternalLoggerFactory {
    val logDir = when (logPath) {
        LogPath.WorkDir -> File(System.getProperty("user.dir"), "logs")
        is LogPath.UserHome -> File(System.getProperty("user.home"), "${logPath.appName}/logs")
        is LogPath.Custom -> File(logPath.path)
    }
    logDir.mkdirs()
    val logFile = File(logDir, "app.log").absolutePath

    val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
    builder.setStatusLevel(Level.WARN)
    builder.setShutdownHook("disable")

    val layout = builder.newLayout("PatternLayout")
        .addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")

    val rootLogger = builder.newRootLogger(Level.DEBUG)

    if (stdout) {
        builder.add(
            builder.newAppender("Console", "CONSOLE")
                .addAttribute("target", "SYSTEM_OUT")
                .add(layout),
        )
        rootLogger.add(builder.newAppenderRef("Console"))
    }

    builder.add(
        builder.newAppender("File", "FILE")
            .addAttribute("fileName", logFile)
            .add(layout),
    )

    builder.add(
        builder.newAppender("AsyncFile", "ASYNC")
            .addComponent(builder.newAppenderRef("File")),
    )

    rootLogger.add(builder.newAppenderRef("AsyncFile"))
    builder.add(rootLogger)

    Configurator.initialize(builder.build())

    return object : ExternalLoggerFactory {
        override fun create(tag: String): ExternalLogger = Log4jExternalLogger(LogManager.getLogger(tag))
        override fun shutdown() {
            LogManager.shutdown()
        }
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
