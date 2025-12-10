package ru.vladislavsumin.core.coroutines.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

@Suppress("PropertyName", "VariableNaming")
public interface VsDispatchers {
    public val Main: MainCoroutineDispatcher
    public val Default: CoroutineDispatcher
    public val Unconfined: CoroutineDispatcher
    public val IO: CoroutineDispatcher
}

internal class VsDispatchersImpl : VsDispatchers {
    override val Main: MainCoroutineDispatcher get() = Dispatchers.Main
    override val Default: CoroutineDispatcher get() = Dispatchers.Default
    override val Unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
    override val IO: CoroutineDispatcher get() = getDefaultIoDispatcher()
}

internal expect fun getDefaultIoDispatcher(): CoroutineDispatcher
