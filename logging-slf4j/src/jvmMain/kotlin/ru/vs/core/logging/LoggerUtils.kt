package ru.vs.core.logging

import co.touchlab.kermit.Logger
import org.apache.logging.log4j.LogManager
import ru.vs.core.logging.KermitLog4jWriter

fun Logger.setupDefault() {
    Logger.setLogWriters(KermitLog4jWriter())
}

fun Logger.shutdown() {
    LogManager.shutdown()
}
