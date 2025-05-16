package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.ScreenParams
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Ключ экрана.
 * @param P тип параметров экрана.
 * @param key сырой тип ключа.
 */
@JvmInline
@InternalNavigationApi
public value class ScreenKey<P : ScreenParams>(public val key: KClass<P>)

internal fun <T : ScreenParams> T.asKey(): ScreenKey<T> = ScreenKey(this::class) as ScreenKey<T>

internal fun ScreenParams.asErasedKey(): ScreenKey<ScreenParams> = ScreenKey(this::class) as ScreenKey<ScreenParams>
