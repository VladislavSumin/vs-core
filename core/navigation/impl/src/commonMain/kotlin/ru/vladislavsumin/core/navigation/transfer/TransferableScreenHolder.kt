package ru.vladislavsumin.core.navigation.transfer

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
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
    val key: Any,
    savedState: SerializableContainer? = null,
    instanceKeeper: InstanceKeeperDispatcher? = null,
) : InstanceKeeper.Instance {

    val lifecycle: LifecycleRegistry = LifecycleRegistry()
    val instanceKeeper: InstanceKeeperDispatcher = instanceKeeper ?: InstanceKeeperDispatcher()
    val stateKeeper: StateKeeperDispatcher = StateKeeperDispatcher(savedState)
    val backHandler: BackDispatcher = BackDispatcher()

    lateinit var screen: GenericScreen<Ctx>
    lateinit var navigator: ScreenNavigatorImpl<Ctx>

    private var boundHostInstanceKeeper: InstanceKeeper? = null
    private var boundHostStateKeeper: StateKeeper? = null
    private var boundHostLifecycle: Lifecycle? = null
    private val mirror = HolderLifecycleMirror()
    private var stateKey: String = ""

    fun createContext(
        factory: com.arkivanov.decompose.ComponentContextFactory<Ctx>,
    ): Ctx = factory(lifecycle, stateKeeper, instanceKeeper, backHandler)

    /**
     * Привязывает holder к mount-контексту: регистрирует stateKeeper и подписывается на lifecycle.
     * Владелец (caller) сам помещает holder в host.instanceKeeper через getOrCreate / put.
     */
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

        host.lifecycle.subscribe(mirror)
    }

    /**
     * Отвязывает holder от текущего mount'а без уничтожения (для переноса).
     */
    fun unbind() {
        val hostInstanceKeeper = boundHostInstanceKeeper
        val hostStateKeeper = boundHostStateKeeper
        val hostLifecycle = boundHostLifecycle

        if (hostLifecycle != null) {
            hostLifecycle.unsubscribe(mirror)
        }
        if (hostStateKeeper != null && stateKey.isNotEmpty()) {
            hostStateKeeper.unregister(stateKey)
        }
        if (hostInstanceKeeper != null) {
            hostInstanceKeeper.remove(key)
        }

        lifecycle.stop()

        boundHostInstanceKeeper = null
        boundHostStateKeeper = null
        boundHostLifecycle = null
    }

    override fun onDestroy() {
        if (::navigator.isInitialized) {
            navigator.detachFromParent()
        }
        lifecycle.destroy()
        instanceKeeper.destroy()
    }

    fun destroyWithoutInstanceKeeper() {
        if (::navigator.isInitialized) {
            navigator.detachFromParent()
        }
        lifecycle.destroy()
        // instanceKeeper НЕ уничтожаем — он передан в новый holder
    }

    private inner class HolderLifecycleMirror : Lifecycle.Callbacks {
        override fun onCreate() = lifecycle.create()
        override fun onStart() = lifecycle.start()
        override fun onResume() = lifecycle.resume()
        override fun onPause() = lifecycle.pause()
        override fun onStop() = lifecycle.stop()
        override fun onDestroy() {
            // onDestroy from mount means removal or config change — not full teardown.
            // Full teardown is driven by TransferableScreenHolder.onDestroy().
        }
    }
}
