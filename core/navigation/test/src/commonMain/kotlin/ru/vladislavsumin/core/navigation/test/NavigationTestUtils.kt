package ru.vladislavsumin.core.navigation.test

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.decompose.test.createTestViewModel
import ru.vladislavsumin.core.navigation.InternalNavigationApi
import ru.vladislavsumin.core.navigation.viewModel.IsNavigationViewModelConstructing
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel

@OptIn(InternalNavigationApi::class)
public fun <T : NavigationViewModel> createTestNavigationViewModel(
    stateKeeper: StateKeeper = StateKeeperDispatcher(),
    uiLifecycle: MutableStateFlow<Lifecycle.State> = MutableStateFlow(Lifecycle.State.RESUMED),
    factory: () -> T,
): T = createTestViewModel(stateKeeper, uiLifecycle) {
    try {
        IsNavigationViewModelConstructing = true
        factory()
    } finally {
        IsNavigationViewModelConstructing = false
    }
}
