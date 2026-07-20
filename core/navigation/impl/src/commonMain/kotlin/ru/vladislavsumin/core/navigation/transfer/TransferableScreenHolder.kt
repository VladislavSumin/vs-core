package ru.vladislavsumin.core.navigation.transfer

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import ru.vladislavsumin.core.navigation.navigator.ScreenNavigatorImpl
import ru.vladislavsumin.core.navigation.screen.GenericScreen

internal class TransferableScreenHolder<Ctx : GenericComponentContext<Ctx>>(
    savedState: SerializableContainer? = null,
    instanceKeeper: InstanceKeeperDispatcher? = null,
    restoredSaveable: Map<String, List<Any?>>? = null,
) {

    val lifecycle = LifecycleRegistry()
    val instanceKeeper = instanceKeeper ?: InstanceKeeperDispatcher()
    val stateKeeper = StateKeeperDispatcher(savedState)
    val backHandler = BackDispatcher()

    val saveableStateRegistry = SaveableStateRegistryImpl(restoredSaveable ?: emptyMap())

    lateinit var screen: GenericScreen<Ctx>
    lateinit var navigator: ScreenNavigatorImpl<Ctx>

    private var boundHostInstanceKeeper: InstanceKeeper? = null
    private var boundHostStateKeeper: StateKeeper? = null
    private var boundHostLifecycle: Lifecycle? = null
    private val mirror = HolderLifecycleMirror()
    private var stateKey: String = ""

    fun createContext(factory: com.arkivanov.decompose.ComponentContextFactory<Ctx>): Ctx =
        factory(lifecycle, stateKeeper, instanceKeeper, backHandler)

    fun bindTo(host: Ctx, stateKey: String) {
        this.stateKey = stateKey
        boundHostInstanceKeeper = host.instanceKeeper
        boundHostStateKeeper = host.stateKeeper
        boundHostLifecycle = host.lifecycle

        if (!host.stateKeeper.isRegistered(stateKey)) {
            host.stateKeeper.register(
                key = stateKey,
                strategy = SerializableContainer.serializer(),
                supplier = { stateKeeper.save() },
            )
        }
        host.instanceKeeper.getOrCreate(stateKey) { InstanceKeeperHolder(instanceKeeper) }

        host.lifecycle.subscribe(mirror)
    }

    fun unbind() {
        val hostInstanceKeeper = boundHostInstanceKeeper
        val hostStateKeeper = boundHostStateKeeper
        val hostLifecycle = boundHostLifecycle

        hostLifecycle?.unsubscribe(mirror)
        if (hostStateKeeper != null && stateKey.isNotEmpty()) {
            hostStateKeeper.unregister(stateKey)
        }
        hostInstanceKeeper?.remove(stateKey)

        lifecycle.stop()

        boundHostInstanceKeeper = null
        boundHostStateKeeper = null
        boundHostLifecycle = null
    }

    fun destroyWithoutInstanceKeeper() {
        if (::navigator.isInitialized) {
            navigator.detachFromParent()
        }
        lifecycle.destroy()
    }

    private inner class HolderLifecycleMirror : Lifecycle.Callbacks {
        override fun onCreate() = lifecycle.create()
        override fun onStart() = lifecycle.start()
        override fun onResume() = lifecycle.resume()
        override fun onPause() = lifecycle.pause()
        override fun onStop() = lifecycle.stop()
        override fun onDestroy() {
            /* empty */
        }
    }

    internal class InstanceKeeperHolder(val dispatcher: InstanceKeeperDispatcher) : InstanceKeeper.Instance {
        override fun onDestroy() {
            dispatcher.destroy()
        }
    }
}
