package ru.vladislavsumin.core.decompose.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Компонент, экспонирующий [resubscribeOnUiLifecycle] и [relaunchOnUiLifecycle] для тестов.
 */
class LifecycleTestComponent(context: ComponentContext) : Component(context) {
    val source = MutableStateFlow(0)

    val resubscribed: Flow<Int> = source.resubscribeOnUiLifecycle(Lifecycle.State.RESUMED)

    val relaunchStartCount = MutableStateFlow(0)
    val relaunchCancelCount = MutableStateFlow(0)

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            relaunchStartCount.value += 1
            try {
                awaitCancellation()
            } finally {
                relaunchCancelCount.value += 1
            }
        }
    }
}

/**
 * ViewModel, экспонирующая [resubscribeOnUiLifecycle] и [relaunchOnUiLifecycle] для тестов.
 */
class LifecycleTestViewModel : ViewModel() {
    val source = MutableStateFlow(0)

    val resubscribed: Flow<Int> = source.resubscribeOnUiLifecycle(Lifecycle.State.RESUMED)

    val relaunchStartCount = MutableStateFlow(0)
    val relaunchCancelCount = MutableStateFlow(0)

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            relaunchStartCount.value += 1
            try {
                awaitCancellation()
            } finally {
                relaunchCancelCount.value += 1
            }
        }
    }
}

/**
 * Компонент-владелец [LifecycleTestViewModel].
 */
class LifecycleTestViewModelComponent(context: ComponentContext) : Component(context) {
    val viewModel = viewModel { LifecycleTestViewModel() }
}
