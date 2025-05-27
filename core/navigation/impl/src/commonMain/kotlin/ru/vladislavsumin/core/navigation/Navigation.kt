package ru.vladislavsumin.core.navigation

import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.collections.tree.asSequence
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.repository.NavigationRepositoryImpl
import ru.vladislavsumin.core.navigation.serializer.NavigationSerializer
import ru.vladislavsumin.core.navigation.tree.NavigationTree
import ru.vladislavsumin.core.navigation.tree.NavigationTreeBuilder

/**
 * Точка входа в навигацию, она же глобальный навигатор.
 */
public class Navigation internal constructor(
    @InternalNavigationApi
    public val navigationTree: NavigationTree,
    internal val navigationSerializer: NavigationSerializer,
) {
    internal val navigationChannel = Channel<NavigationEvent>(Channel.BUFFERED)

    public fun open(screenParams: IntentScreenParams<ScreenIntent>): Unit = send(NavigationEvent.Open(screenParams))
    public fun close(screenParams: IntentScreenParams<ScreenIntent>): Unit = send(NavigationEvent.Close(screenParams))

    private fun send(event: NavigationEvent) {
        navigationChannel.trySend(event).getOrThrow()
    }

    /**
     * Ищет параметры экрана по их имени. Можно использовать для реализации отладочных ссылок.
     * **Внимание** Названия параметров могут быть изменены при минимизации приложения, поэтому данный метод не будет
     * работать в релизе.
     */
    public fun findDefaultScreenParamsByName(name: String): IntentScreenParams<ScreenIntent>? {
        return navigationTree
            .asSequence()
            .find { it.value.screenKey.key.simpleName == name }
            ?.value
            ?.defaultParams
    }

    internal sealed interface NavigationEvent {
        data class Open(val screenParams: IntentScreenParams<ScreenIntent>) : NavigationEvent
        data class Close(val screenParams: IntentScreenParams<ScreenIntent>) : NavigationEvent
    }

    public companion object {
        public operator fun invoke(
            registrars: Set<NavigationRegistrar>,
        ): Navigation {
            val navigationRepository = NavigationRepositoryImpl(registrars)
            val navigationSerializer = NavigationSerializer(navigationRepository)
            val navigationTreeBuilder = NavigationTreeBuilder(navigationRepository)
            val navigationTree = navigationTreeBuilder.build()
            return Navigation(navigationTree, navigationSerializer)
        }
    }
}
