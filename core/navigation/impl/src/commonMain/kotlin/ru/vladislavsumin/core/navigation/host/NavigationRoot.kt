package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.decompose.components.utils.createCoroutineScope
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.Navigation.NavigationEvent
import ru.vladislavsumin.core.navigation.NavigationLogger
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.GlobalNavigator
import ru.vladislavsumin.core.navigation.navigator.ScreenNavigator
import ru.vladislavsumin.core.navigation.screen.DefaultScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenPath

/**
 * Корневая точка входа в навигацию.
 *
 * @param navigation граф навигации.
 * @param key уникальный в пределах компонента ключ дочернего компонента.
 * @param coroutineScope scope на котором будут запускаться асинхронные задачи навигации в этом экране.
 * @param onContentReady если передано не нулевое значение, то будут вызваны методы
 * [ru.vs.core.navigation.screen.Screen.delaySplashScreen] в цепочке открываемых экранов и после того как все вызовы
 * завершатся, будет вызван этот callback. Таким образом можно задерживать splash экран в обычных экранах.
 */
public fun ComponentContext.childNavigationRoot(
    navigation: Navigation,
    key: String = "navigation-root",
    coroutineScope: CoroutineScope = lifecycle.createCoroutineScope(),
    onContentReady: (() -> Unit)? = null,
): ComposeComponent {
    val node = navigation.navigationTree
    val params = node.value.defaultParams ?: error("Root screen must have default params")
    val rootScreenFactory = node.value.factory as ScreenFactory<IntentScreenParams<ScreenIntent>, ScreenIntent, *>?
    check(rootScreenFactory != null) { "Factory for $params not found" }

    // Создаем рутовый навигатор.
    val globalNavigator = GlobalNavigator(navigation)

    val initialPath = handleInitialNavigationEvent(params, navigation, globalNavigator)

    // Создаем дочерний контекст который будет являться контекстом для корневого экрана графа навигации.
    // Lifecycle полученного компонента будет совпадать с родителем
    val childContext = childContext(key, lifecycle = null)

    val rootScreenNavigator = ScreenNavigator(
        globalNavigator = globalNavigator,
        parentNavigator = null,
        screenPath = ScreenPath(params),
        node = node,
        serializer = navigation.navigationSerializer.serializer,
        lifecycle = childContext.lifecycle,
        initialPath = initialPath,
    )

    globalNavigator.rootNavigator = rootScreenNavigator

    val rootScreenContext = DefaultScreenContext(
        rootScreenNavigator,
        childContext,
    )

    // TODO поддержать события для root экрана.
    val screen = rootScreenFactory.create(rootScreenContext, params, Channel())
    rootScreenNavigator.screen = screen

    handleNavigation(navigation, rootScreenContext)

    // Обрабатываем задержку splash экрана.
    if (onContentReady != null) {
        coroutineScope.launch {
            rootScreenNavigator.delaySplashScreen()
            onContentReady()
        }
    }

    return screen
}

private fun handleInitialNavigationEvent(
    rootScreenParams: IntentScreenParams<ScreenIntent>,
    navigation: Navigation,
    globalNavigator: GlobalNavigator,
): ScreenPath? {
    val initialNavigationParams = navigation.navigationChannel.tryReceive().getOrNull()
    NavigationLogger.d { "childNavigationRoot() initialNavigationParams = $initialNavigationParams" }

    return when (initialNavigationParams) {
        is NavigationEvent.Close -> {
            // Можно поддержать, но пока нет такой необходимости.
            error("Unsupported initial close event")
        }

        is NavigationEvent.Open -> globalNavigator.createOpenPath(
            ScreenPath(rootScreenParams),
            initialNavigationParams.screenParams,
        )

        null -> null
    }
}

/**
 * Обрабатывает глобальную навигацию из [navigation].
 */
private fun ComponentContext.handleNavigation(
    navigation: Navigation,
    screenContext: ScreenContext,
) {
    val scope = lifecycle.createCoroutineScope()
    scope.launch {
        for (event in navigation.navigationChannel) {
            NavigationLogger.d { "Handle global navigation event $event" }
            when (event) {
                is NavigationEvent.Open -> screenContext.navigator.open(
                    event.screenParams as IntentScreenParams<ScreenIntent>,
                    event.intent as ScreenIntent?,
                )

                is NavigationEvent.Close -> screenContext.navigator.close(event.screenParams)
            }
        }
    }
}
