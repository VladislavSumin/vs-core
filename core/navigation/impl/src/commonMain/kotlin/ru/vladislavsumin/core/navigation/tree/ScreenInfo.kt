package ru.vladislavsumin.core.navigation.tree

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * @param hostInParent хост родительского экрана в котором располагается этот экран, null только для рут экрана.
 */
@InternalNavigationApi
public data class ScreenInfo<Ctx : GenericComponentContext<Ctx>>(
    val screenKey: ScreenKey,
    val factory: ScreenFactory<Ctx, *, *, *>?,
    val defaultParams: IntentScreenParams<*>?,
    val description: String?,
    val hostInParent: NavigationHost?,
    val navigationHosts: Set<NavigationHost>,
)
