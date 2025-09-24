package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import ru.vladislavsumin.core.decompose.components.utils.createCoroutineScope
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.GenericNavigation.NavigationEvent
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationLogger
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.GlobalNavigator
import ru.vladislavsumin.core.navigation.navigator.ScreenNavigator
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenNavigatorHolder
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.ScreenPathWithIntent

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
public fun <Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>> Ctx.childNavigationRoot(
    navigation: GenericNavigation<Ctx, BS>,
    key: String = "navigation-root",
    coroutineScope: CoroutineScope = lifecycle.createCoroutineScope(),
    onContentReady: (() -> Unit)? = null,
): BS {
    val node = navigation.navigationTree
    val params = node.value.defaultParams ?: error("Root screen must have default params")
    val rootScreenFactory =
        node.value.factory as ScreenFactory<Ctx, IntentScreenParams<ScreenIntent>, ScreenIntent, BS, *>?
    check(rootScreenFactory != null) { "Factory for $params not found" }

    // Создаем рутовый навигатор.
    val globalNavigator = GlobalNavigator(navigation)

    // Считываем тут первое событие навигации на момент вызова корня навигации
    // Это нужно если мы хотим сразу отобразить отличную от стандартной иерархию экранов без создания
    // иерархии по умолчанию, например, это может использоваться при открытии приложения через deepLink.
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

    // При создании root хоста relay внутри globalNavigator гарантированно пуст, поэтому мы можем быть уверены,
    // что установка значения сюда пройдет синхронно.
    var screen: BS? = null

    globalNavigator.protectRestoreState {
        screen = try {
            ScreenNavigatorHolder = rootScreenNavigator
            // TODO поддержать события для root экрана.
            rootScreenFactory.create(childContext, params, Channel())
        } finally {
            ScreenNavigatorHolder = null
        }
        rootScreenNavigator.screen = screen
    }

    handleNavigation(navigation, rootScreenNavigator)

    // Обрабатываем задержку splash экрана.
    if (onContentReady != null) {
        coroutineScope.launch {
            rootScreenNavigator.delaySplashScreen()
            onContentReady()
        }
    }

    return screen ?: error("Unreachable, screen can't be null")
}

private fun <Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>> handleInitialNavigationEvent(
    rootScreenParams: IntentScreenParams<ScreenIntent>,
    navigation: GenericNavigation<Ctx, BS>,
    globalNavigator: GlobalNavigator<Ctx, BS>,
): ScreenPathWithIntent? {
    val initialNavigationParams = navigation.navigationChannel.tryReceive().getOrNull()
    NavigationLogger.d { "childNavigationRoot() initialNavigationParams = $initialNavigationParams" }

    val initialPath = when (initialNavigationParams) {
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

    return initialPath?.let { ScreenPathWithIntent(it, initialNavigationParams?.intent) }
}

/**
 * Обрабатывает глобальную навигацию из [navigation].
 */
private fun <Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>> Ctx.handleNavigation(
    navigation: GenericNavigation<Ctx, BS>,
    rootScreenNavigator: ScreenNavigator<Ctx, BS>,
) {
    val scope = lifecycle.createCoroutineScope()
    scope.launch {
        for (event in navigation.navigationChannel) {
            NavigationLogger.d { "Handle global navigation event $event" }
            when (event) {
                is NavigationEvent.Open -> rootScreenNavigator.open(
                    event.screenParams as IntentScreenParams<ScreenIntent>,
                    event.intent as ScreenIntent?,
                )

                is NavigationEvent.Close -> rootScreenNavigator.close(event.screenParams)
            }
        }
    }
}
