package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.manager.ExternalLoggerFactory

internal expect fun createPlatformLoggerFactory(logPath: LogPath, stdout: Boolean = true): ExternalLoggerFactory
