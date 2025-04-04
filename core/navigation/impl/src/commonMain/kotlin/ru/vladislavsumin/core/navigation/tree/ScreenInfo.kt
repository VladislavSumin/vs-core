package ru.vladislavsumin.core.navigation.tree

import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

internal data class ScreenInfo(
    val screenKey: ScreenKey<*>,
    val factory: ScreenFactory<*, *>?,
    val defaultParams: ScreenParams?,
    val description: String?,
    val hostInParent: NavigationHost?,
    val navigationHosts: Set<NavigationHost>,
)
