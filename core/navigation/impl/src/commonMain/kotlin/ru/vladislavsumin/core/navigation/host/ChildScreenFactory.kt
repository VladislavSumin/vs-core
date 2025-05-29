package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.screen.wrapWithScreenContext

/**
 * Стандартная фабрика дочерних экранов для использования в compose навигации.
 */
internal fun ScreenContext.childScreenFactory(
    configuration: ConfigurationHolder,
    context: ComponentContext,
): Screen {
    val screenContext = context.wrapWithScreenContext(navigator, configuration.screenParams)
    val screenFactory = navigator.getChildScreenFactory(configuration.screenParams.asKey())
    val screen = screenFactory.create(
        context = screenContext,
        params = configuration.screenParams as IntentScreenParams<ScreenIntent>,
        intents = configuration.intents,
    )
    screenContext.navigator.screen = screen
    return screen
}
