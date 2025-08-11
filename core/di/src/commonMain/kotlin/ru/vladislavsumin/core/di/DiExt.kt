package ru.vladislavsumin.core.di

import org.kodein.di.DirectDIAware
import org.kodein.di.instance

/**
 * Короткий alias для instance().
 */
public inline fun <reified T : Any> DirectDIAware.i(tag: Any? = null): T = instance(tag)
