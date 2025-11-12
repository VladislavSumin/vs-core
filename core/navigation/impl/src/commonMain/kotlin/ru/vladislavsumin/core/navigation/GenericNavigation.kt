package ru.vladislavsumin.core.navigation

import com.arkivanov.decompose.GenericComponentContext
import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.collections.tree.asSequence
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.repository.NavigationRepositoryImpl
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.serializer.NavigationSerializer
import ru.vladislavsumin.core.navigation.tree.NavigationTree
import ru.vladislavsumin.core.navigation.tree.NavigationTreeBuilder

/**
 * Точка входа в навигацию, она же глобальный навигатор.
 */
public class GenericNavigation<Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>> internal constructor(
    @InternalNavigationApi
    public val navigationTree: NavigationTree<Ctx, BS>,
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
        data class Open(val screenParams: IntentScreenParams<*>, val intent: ScreenIntent?) : NavigationEvent
        data class Close(val screenParams: IntentScreenParams<*>) : NavigationEvent
    }

    public companion object {
        public operator fun <Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>> invoke(
            registrars: Set<GenericNavigationRegistrar<Ctx, BS>>,
        ): GenericNavigation<Ctx, BS> {
            val navigationRepository = NavigationRepositoryImpl(registrars)
            val navigationSerializer = NavigationSerializer(navigationRepository)
            val navigationTreeBuilder = NavigationTreeBuilder(navigationRepository)
            val navigationTree = navigationTreeBuilder.build()
            return GenericNavigation(navigationTree, navigationSerializer)
        }
    }
}
