package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.ScreenIntent
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Ключ экрана.
 * @param key сырой тип ключа.
 */
@JvmInline
@InternalNavigationApi
public value class ScreenKey(public val key: KClass<out IntentScreenParams<out ScreenIntent>>)

internal fun <T : IntentScreenParams<I>, I : ScreenIntent> T.asKey(): ScreenKey = ScreenKey(this::class)
