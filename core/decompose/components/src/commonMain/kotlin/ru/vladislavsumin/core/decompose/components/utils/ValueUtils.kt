package ru.vladislavsumin.core.decompose.components.utils

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Преобразует [Value] в [StateFlow].
 */
public fun <T : Any> Value<T>.asStateFlow(): StateFlow<T> = ValueStateFlow(store = this)
private class ValueStateFlow<T : Any>(private val store: Value<T>) : StateFlow<T> {
    override val value: T get() = store.value
    override val replayCache: List<T> get() = listOf(store.value)

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        val flow = MutableStateFlow(store.value)
        val sub = store.subscribe { flow.value = it }
        try {
            flow.collect(collector)
        } finally {
            sub.cancel()
        }
    }
}
