package ru.vs.iot.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

internal class KermitLog4jWriter : LogWriter() {
    private val logger = LogManager.getLogger("Kermit")

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (throwable == null) {
            logger.log(severity.toLevel(), "[$tag] $message")
        } else {
            logger.log(severity.toLevel(), "[$tag] $message", throwable)
        }
    }

    private fun Severity.toLevel(): Level = when (this) {
        Severity.Verbose -> Level.TRACE
        Severity.Debug -> Level.DEBUG
        Severity.Info -> Level.INFO
        Severity.Warn -> Level.WARN
        Severity.Error -> Level.ERROR
        Severity.Assert -> Level.FATAL
    }
}
