package ru.vladislavsumin.core.navigation.tree

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

@InternalNavigationApi
public data class ScreenInfo(
    val screenKey: ScreenKey,
    val factory: ScreenFactory<*, *, *>?,
    val defaultParams: IntentScreenParams<ScreenIntent>?,
    val description: String?,
    val hostInParent: NavigationHost?,
    val navigationHosts: Set<NavigationHost>,
)
