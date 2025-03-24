package ru.vladislavsumin.core.logger.platform

import ru.vladislavsumin.core.logger.manager.ExternalLoggerFactory

internal expect fun createPlatformLoggerFactory(): ExternalLoggerFactory
