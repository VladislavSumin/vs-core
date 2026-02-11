package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Ключ экрана.
 * @param key сырой тип ключа.
 */
@JvmInline
@InternalNavigationApi
public value class ScreenKey @PublishedApi internal constructor(public val key: KClass<out IntentScreenParams<*>>)

internal fun IntentScreenParams<*>.asKey(): ScreenKey = ScreenKey(this::class)
