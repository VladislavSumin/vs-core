package ru.vladislavsumin.core.decompose.test

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.decompose.components.InternalDecomposeApi
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.decompose.components.WhileConstructedViewModelStateKeeper
import ru.vladislavsumin.core.decompose.components.WhileConstructedViewModelUiLifecycle

@OptIn(InternalDecomposeApi::class)
public fun <T : ViewModel> createTestViewModel(
    stateKeeper: StateKeeper = StateKeeperDispatcher(),
    uiLifecycle: MutableStateFlow<Lifecycle.State> = MutableStateFlow(Lifecycle.State.RESUMED),
    factory: () -> T,
): T {
    WhileConstructedViewModelStateKeeper = stateKeeper
    WhileConstructedViewModelUiLifecycle = uiLifecycle
    try {
        return factory()
    } finally {
        WhileConstructedViewModelStateKeeper = null
        WhileConstructedViewModelUiLifecycle = null
    }
}
