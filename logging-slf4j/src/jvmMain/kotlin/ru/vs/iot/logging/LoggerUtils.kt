package ru.vs.iot.logging

import co.touchlab.kermit.Logger
import org.apache.logging.log4j.LogManager

fun Logger.setupDefault() {
    Logger.setLogWriters(KermitLog4jWriter())
}

fun Logger.shutdown() {
    LogManager.shutdown()
}
