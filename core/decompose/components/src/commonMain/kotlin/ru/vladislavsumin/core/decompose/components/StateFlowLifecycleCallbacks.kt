package ru.vladislavsumin.core.decompose.components

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow

internal class StateFlowLifecycleCallbacks(
    private val flow: MutableStateFlow<Lifecycle.State>,
) : Lifecycle.Callbacks {
    override fun onCreate() {
        flow.value = Lifecycle.State.CREATED
    }

    override fun onDestroy() {
        flow.value = Lifecycle.State.DESTROYED
    }

    override fun onPause() {
        flow.value = Lifecycle.State.STARTED
    }

    override fun onResume() {
        flow.value = Lifecycle.State.RESUMED
    }

    override fun onStart() {
        flow.value = Lifecycle.State.STARTED
    }

    override fun onStop() {
        flow.value = Lifecycle.State.CREATED
    }
}
