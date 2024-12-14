package ru.vladislavsumin.core.decompose.components.utils

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Создает [CoroutineScope] связанный с [Lifecycle]. [CoroutineScope] будет автоматически закрыт
 * при переходе [Lifecycle] в состояние [Lifecycle.State.DESTROYED].
 */
public fun Lifecycle.createCoroutineScope(context: CoroutineContext = Dispatchers.Main.immediate) =
    LifecycleCoroutineScope(context, this)

private fun LifecycleCoroutineScope(
    context: CoroutineContext,
    lifecycle: Lifecycle,
): CoroutineScope {
    val scope = CoroutineScope(context)
    lifecycle.doOnDestroy(scope::cancel)
    return scope
}
