package ru.vladislavsumin.core.navigation.screen

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.components.GenericComponent
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.ScreenNavigator
import ru.vladislavsumin.core.navigation.viewModel.IsNavigationViewModelConstructing
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel

/**
 * Базовая реализация экрана с набором полезных расширений.
 */
public abstract class GenericScreen<Ctx : GenericComponentContext<Ctx>, BS : GenericScreen<Ctx, BS>>(context: Ctx) :
    GenericComponent<Ctx>(context) {
    /**
     * Предоставляет доступ к навигации с учетом контекста этого экрана.
     *
     * Доступ к контексту означает что поиск ближайшего экрана будет происходить не от корня графа, а от текущего этого
     * экрана.
     */
    protected val navigator: ScreenNavigator<Ctx, BS> = let {
        val navigator = ScreenNavigatorHolder
        check(navigator != null) { "Wrong screen usage, only navigation framework may create screen instances" }
        navigator as ScreenNavigator<Ctx, BS>
    }

    internal val internalNavigator: ScreenNavigator<Ctx, BS> get() = navigator
    internal val internalContext: Ctx get() = context

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
        try {
            IsNavigationViewModelConstructing = true
            val viewModel = super.viewModel(factory)
            (viewModel as? NavigationViewModel)?.handleNavigation()
            return viewModel
        } finally {
            IsNavigationViewModelConstructing = false
        }
    }

    /**
     * Регистрирует кастомную фабрику для экрана [T]. Данный экран должен открываться в хостах навигации этого экрана.
     * **Внимание** Регистрировать фабрики нужно ДО объявления хостов навигации. Это важно при восстановлении состояния.
     */
    protected inline fun <
        reified T : IntentScreenParams<I>,
        I : ScreenIntent,
        S : BS,
        > registerCustomFactory(
        factory: ScreenFactory<Ctx, T, I, BS, S>,
    ) {
        navigator.registerCustomFactory(ScreenKey(T::class), factory)
    }

    @PublishedApi
    internal fun NavigationViewModel.handleNavigation(): Unit = launch {
        for (event in navigationChannel) {
            when (event) {
                is NavigationViewModel.NavigationEvent.Open -> navigator.open(
                    event.screenParams as IntentScreenParams<ScreenIntent>,
                    event.intent as ScreenIntent?,
                )

                is NavigationViewModel.NavigationEvent.Close -> navigator.close(event.screenParams)
                NavigationViewModel.NavigationEvent.CloseSelf -> navigator.close()
            }
        }
    }
}
