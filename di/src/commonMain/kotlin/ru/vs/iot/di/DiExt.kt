package ru.vs.iot.di

import org.kodein.di.DirectDIAware
import org.kodein.di.instance

inline fun <reified T : Any> DirectDIAware.i(tag: Any? = null): T = instance(tag)
