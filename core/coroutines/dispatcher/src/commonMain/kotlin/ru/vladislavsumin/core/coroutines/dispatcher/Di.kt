package ru.vladislavsumin.core.coroutines.dispatcher

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import ru.vladislavsumin.core.di.Modules

public fun Modules.coreCoroutinesDispatchers(): DI.Module = DI.Module("core-coroutines-dispatchers") {
    bindSingleton<VsDispatchers> { VsDispatchersImpl() }
}
