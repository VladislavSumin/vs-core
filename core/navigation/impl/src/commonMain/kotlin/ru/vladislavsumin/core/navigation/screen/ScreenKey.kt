package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.ScreenParams
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Ключ экрана.
 * @param key сырой тип ключа.
 */
@JvmInline
@InternalNavigationApi
public value class ScreenKey(public val key: KClass<out ScreenParams>)

internal fun <T : ScreenParams> T.asKey(): ScreenKey = ScreenKey(this::class)
