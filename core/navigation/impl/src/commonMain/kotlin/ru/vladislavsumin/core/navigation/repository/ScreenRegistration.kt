package ru.vladislavsumin.core.navigation.repository

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Содержит информацию о регистрации экрана.
 *
 * Обратите внимание, хоть информация о шаблонах в этом классе и стирается, но все параметры должны быть
 * совместимых типов.
 *
 * @param factory фабрика для создания компонента экрана.
 * @param defaultParams параметры экрана по умолчанию.
 * @param navigationHosts список [NavigationHost] и их экранов, которые открываются с этого экрана.
 * @param description опциональное описание экрана для отладки.
 */
internal data class ScreenRegistration<Ctx : GenericComponentContext<Ctx>>(
    val factory: ScreenFactory<Ctx, *, *, *>?,
    val defaultParams: IntentScreenParams<*>?,
    val navigationHosts: Map<NavigationHost, Set<ScreenKey>>,
    val description: String?,
)
