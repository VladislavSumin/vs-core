package ru.vladislavsumin.core.navigation.repository

import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

/**
 * Содержит информацию о регистрации экрана.
 *
 * @param P тип параметров экрана.
 * @param S тип экрана.
 *
 * @param factory фабрика для создания компонента экрана.
 * @param defaultParams параметры экрана по умолчанию.
 * @param opensIn список навигационных хостов в которых может открываться этот экран.
 * @param navigationHosts список [NavigationHost] которые открываются с этого экрана.
 * @param description опциональное описание экрана для дебага.
 */
internal data class ScreenRegistration<P : ScreenParams, S : Screen>(
    val factory: ScreenFactory<P, S>?,
    val defaultParams: P?,
    val opensIn: Set<NavigationHost>,
    val navigationHosts: Set<NavigationHost>,
    val description: String?,
)
