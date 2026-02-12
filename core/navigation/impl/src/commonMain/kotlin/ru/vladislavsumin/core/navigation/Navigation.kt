package ru.vladislavsumin.core.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.GenericComponentContext
import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.collections.tree.asSequence
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.repository.NavigationRepositoryImpl
import ru.vladislavsumin.core.navigation.serializer.NavigationSerializer
import ru.vladislavsumin.core.navigation.tree.NavigationTree
import ru.vladislavsumin.core.navigation.tree.NavigationTreeBuilder

public typealias Navigation = GenericNavigation<ComponentContext>

/**
 * Точка входа в навигацию, она же глобальный навигатор.
 */
public class GenericNavigation<Ctx : GenericComponentContext<Ctx>> internal constructor(
    @InternalNavigationApi
    public val navigationTree: NavigationTree<Ctx>,
    internal val navigationSerializer: NavigationSerializer,
) {
    internal val navigationChannel = Channel<NavigationEvent>(Channel.BUFFERED)

    public fun <S : IntentScreenParams<I>, I : ScreenIntent> open(screenParams: S, intent: I? = null): Unit =
        send(NavigationEvent.Open(screenParams, intent))

    public fun close(screenParams: IntentScreenParams<ScreenIntent>): Unit = send(NavigationEvent.Close(screenParams))

    private fun send(event: NavigationEvent) {
        navigationChannel.trySend(event).getOrThrow()
    }

    /**
     * Ищет параметры экрана по их имени. Можно использовать для реализации отладочных ссылок.
     *
     * **Внимание!** Названия параметров могут быть изменены при минимизации приложения, поэтому данный метод не будет
     * работать в релизе.
     */
    public fun findDefaultScreenParamsByName(name: String): IntentScreenParams<*>? {
        return navigationTree
            .asSequence()
            .map { it.value }
            .find { it.screenKey.key.simpleName == name }
            ?.defaultParams
    }

    internal sealed interface NavigationEvent {
        data class Open(val screenParams: IntentScreenParams<*>, val intent: ScreenIntent?) : NavigationEvent
        data class Close(val screenParams: IntentScreenParams<*>) : NavigationEvent
    }

    public companion object {
        public operator fun <Ctx : GenericComponentContext<Ctx>> invoke(
            registrars: Set<GenericNavigationRegistrar<Ctx>>,
        ): GenericNavigation<Ctx> {
            val navigationRepository = NavigationRepositoryImpl(registrars)
            val navigationSerializer = NavigationSerializer(navigationRepository)
            val navigationTreeBuilder = NavigationTreeBuilder(navigationRepository)
            val navigationTree = navigationTreeBuilder.build()
            return GenericNavigation(navigationTree, navigationSerializer)
        }
    }
}
