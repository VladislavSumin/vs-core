package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.Render
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

@InternalNavigationApi
public data class ScreenInfo<Ctx : GenericComponentContext<Ctx>, R : Render>(
    val screenKey: ScreenKey,
    val factory: ScreenFactory<Ctx, *, *, R, *>?,
    val defaultParams: IntentScreenParams<ScreenIntent>?,
    val description: String?,
    val hostInParent: NavigationHost?,
    val navigationHosts: Set<NavigationHost>,
)
