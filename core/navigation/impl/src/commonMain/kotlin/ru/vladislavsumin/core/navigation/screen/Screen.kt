package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel

/**
 * Базовая реализация экрана с набором полезных расширений.
 */
public abstract class Screen(context: ScreenContext) :
    Component(context),
    ComposeComponent,
    ScreenContext,
    BaseScreenContext by context {

    /**
     * Предоставляет искусственно задержать splash экран на время загрузки контента вашего экрана.
     *
     * Эта функция может быть вызвана при холодном старте, если экран открывается сразу или при восстановлении
     * состояния после смерти процесса.
     *
     * Эта функция *не* будет вызвана при обычном переходе на экран, если splash экран уже был закрыт.
     */
    protected open suspend fun delaySplashScreen() {}
    internal suspend fun delaySplashScreenInternal() = delaySplashScreen()

    /**
     * Расширяет стандартную [Component.viewModel] добавляя дополнительный функционал навигации:
     * Если [T] является наследником [NavigationViewModel], то связывает навигацию экрана с навигацией ViewModel.
     */
    final override fun <T : ViewModel> viewModel(factory: () -> T): T {
        val viewModel = super.viewModel(factory)
        (viewModel as? NavigationViewModel)?.handleNavigation()
        return viewModel
    }

    /**
     * Регистрирует кастомную фабрику для экрана [T]. Данный экран должен открываться в хостах навигации этого экрана.
     * **Внимание** Регистрировать фабрики нужно ДО объявления хостов навигации. Это важно при восстановлении состояния.
     */
    protected inline fun <reified T : IntentScreenParams<I>, I : ScreenIntent> registerCustomFactory(
        factory: ScreenFactory<T, I, Screen>,
    ) {
        navigator.registerCustomFactory(ScreenKey(T::class), factory)
    }

    @PublishedApi
    internal fun NavigationViewModel.handleNavigation() = launch {
        for (event in navigationChannel) {
            when (event) {
                is NavigationViewModel.NavigationEvent.Open -> navigator.open(
                    event.screenParams as IntentScreenParams<ScreenIntent>,
                    event.intent as ScreenIntent,
                )

                is NavigationViewModel.NavigationEvent.Close -> navigator.close(event.screenParams)
                NavigationViewModel.NavigationEvent.CloseSelf -> navigator.close()
            }
        }
    }
}
