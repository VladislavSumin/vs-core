package ru.vladislavsumin.core.coroutines.utils

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Аналог функции [Flow.map], но для [StateFlow] с возвратом [StateFlow] вместо [Flow].
 */
public inline fun <T1, R> StateFlow<T1>.mapState(crossinline transform: (a: T1) -> R): StateFlow<R> {
    return DerivedStateFlow(
        getValue = { transform(this.value) },
        flow = this.map { a -> transform(a) },
    )
}

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@PublishedApi
internal class DerivedStateFlow<T>(
    private val getValue: () -> T,
    private val flow: Flow<T>,
) : StateFlow<T> {

    override val replayCache: List<T> get() = listOf(value)

    override val value: T get() = getValue()

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        flow.distinctUntilChanged().collect(collector)
        error("StateFlow collection never ends.")
    }
}
