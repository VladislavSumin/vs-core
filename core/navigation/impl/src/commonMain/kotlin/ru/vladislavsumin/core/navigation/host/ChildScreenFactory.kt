package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.screen.wrapWithScreenContext

/**
 * Стандартная фабрика дочерних экранов для использования в compose навигации.
 */
internal fun ScreenContext.childScreenFactory(
    screenParams: IntentScreenParams<*>,
    context: ComponentContext,
): Screen {
    val screenContext = context.wrapWithScreenContext(navigator, screenParams)
    val screenFactory = navigator.getChildScreenFactory(screenParams.asKey())
    val screen = screenFactory.create(screenContext, screenParams as IntentScreenParams<ScreenIntent>)
    screenContext.navigator.screen = screen
    return screen
}
