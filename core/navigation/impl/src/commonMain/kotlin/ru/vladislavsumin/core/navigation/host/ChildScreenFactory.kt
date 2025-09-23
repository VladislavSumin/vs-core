package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenNavigatorHolder
import ru.vladislavsumin.core.navigation.screen.asKey

/**
 * Стандартная фабрика дочерних экранов для использования в compose навигации.
 */
internal fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.childScreenFactory(
    configuration: ConfigurationHolder,
    childScreenContext: Ctx,
): GenericScreen<Ctx> {
    val childScreenNavigator = internalNavigator.createChildNavigator(
        childScreenParams = configuration.screenParams,
        childContext = childScreenContext,
    )
    val screenFactory = internalNavigator.getChildScreenFactory(configuration.screenParams.asKey())
    val screen = try {
        ScreenNavigatorHolder = childScreenNavigator
        screenFactory.create(
            context = childScreenContext,
            params = configuration.screenParams as IntentScreenParams<ScreenIntent>,
            intents = configuration.intentReceiveChannel,
        )
    } finally {
        ScreenNavigatorHolder = null
    }
    childScreenNavigator.screen = screen
    return screen
}
