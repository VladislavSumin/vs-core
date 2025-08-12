package ru.vladislavsumin.core.navigation.repository

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Содержит информацию о регистрации экрана.
 *
 * @param P тип параметров экрана.
 * @param S тип экрана.
 *
 * @param factory фабрика для создания компонента экрана.
 * @param defaultParams параметры экрана по умолчанию.
 * @param navigationHosts список [NavigationHost] и их экранов, которые открываются с этого экрана.
 * @param description опциональное описание экрана для дебага.
 */
internal data class ScreenRegistration<
    Ctx : GenericComponentContext<Ctx>,
    P : IntentScreenParams<I>,
    I : ScreenIntent,
    S : GenericScreen<Ctx>,
    >(
    val factory: ScreenFactory<Ctx, P, I, S>?,
    val defaultParams: P?,
    val navigationHosts: Map<NavigationHost, Set<ScreenKey>>,
    val description: String?,
)
