package ru.vladislavsumin.core.navigation.viewModel

import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

/**
 * Расширение [ViewModel] с поддержкой навигации внутри вью модели без необходимости писать связку явно.
 * Связывание происходит через [navigationChannel] и [ru.vladislavsumin.core.navigation.screen.Screen.viewModel],
 * поэтому создавать такую вью модель имеет смысл только через эту специальную функцию.
 */
public abstract class NavigationViewModel : ViewModel() {
    internal val navigationChannel: Channel<NavigationEvent> = Channel(capacity = Channel.BUFFERED)

    init {
        check(IsNavigationViewModelConstructing) {
            "Wrong NavigationViewModel usage. This type of view models can be constructed only via Screen.viewModel function"
        }
    }

    /**
     * Работает аналогично [ru.vladislavsumin.core.navigation.navigator.ScreenNavigator.open].
     */
    protected fun <S : IntentScreenParams<I>, I : ScreenIntent> open(screenParams: S, intent: I? = null): Unit =
        send(NavigationEvent.Open(screenParams, intent))

    /**
     * Работает аналогично [ru.vladislavsumin.core.navigation.navigator.ScreenNavigator.close].
     */
    protected fun close(screenParams: IntentScreenParams<*>): Unit =
        send(NavigationEvent.Close(screenParams))

    /**
     * Работает аналогично [ru.vladislavsumin.core.navigation.navigator.ScreenNavigator.close].
     */
    protected fun close(): Unit = send(NavigationEvent.CloseSelf)

    private fun send(event: NavigationEvent) {
        navigationChannel.trySend(event).getOrThrow()
    }

    internal sealed interface NavigationEvent {
        data class Open(val screenParams: IntentScreenParams<*>, val intent: ScreenIntent?) : NavigationEvent
        data class Close(val screenParams: IntentScreenParams<*>) : NavigationEvent
        data object CloseSelf : NavigationEvent
    }
}
