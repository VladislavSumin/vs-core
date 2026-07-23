package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey

internal class FactoryProviderRegistry<Ctx : GenericComponentContext<Ctx>> {

    private val factories = mutableMapOf<ProviderKey, ScreenFactory<Ctx, *, *, *>>()
    private val callbacks = mutableMapOf<ProviderKey, MutableList<(ScreenFactory<Ctx, *, *, *>) -> Unit>>()
    private val dependentNavigators = mutableMapOf<IntentScreenParams<*>, MutableSet<ScreenNavigatorImpl<*>>>()

    fun register(
        providerParams: IntentScreenParams<*>,
        targetKey: ScreenKey,
        factory: ScreenFactory<Ctx, *, *, *>,
    ) {
        val key = ProviderKey(providerParams, targetKey)
        factories[key] = factory
        val pending = callbacks.remove(key)
        if (pending != null) {
            for (cb in pending) {
                cb(factory)
            }
        }
    }

    fun get(providerParams: IntentScreenParams<*>, targetKey: ScreenKey): ScreenFactory<Ctx, *, *, *>? =
        factories[ProviderKey(providerParams, targetKey)]

    fun subscribe(
        providerParams: IntentScreenParams<*>,
        targetKey: ScreenKey,
        onReady: (ScreenFactory<Ctx, *, *, *>) -> Unit,
    ) {
        val key = ProviderKey(providerParams, targetKey)
        val existing = factories[key]
        if (existing != null) {
            onReady(existing)
        } else {
            callbacks.getOrPut(key) { mutableListOf() }.add(onReady)
        }
    }

    fun registerDependent(providerParams: IntentScreenParams<*>, dependentNav: ScreenNavigatorImpl<*>) {
        dependentNavigators.getOrPut(providerParams) { mutableSetOf() }.add(dependentNav)
    }

    fun unregisterDependent(providerParams: IntentScreenParams<*>, dependentNav: ScreenNavigatorImpl<*>) {
        dependentNavigators[providerParams]?.remove(dependentNav)
    }

    fun removeProvider(providerParams: IntentScreenParams<*>) {
        val toRemove = factories.keys.filter { it.providerParams == providerParams }
        toRemove.forEach { factories.remove(it) }
        callbacks.keys.filter { it.providerParams == providerParams }.forEach { callbacks.remove(it) }
        dependentNavigators.remove(providerParams)?.forEach { nav ->
            nav.globalNavigator.close(
                startScreenPath = nav.screenPath,
                targetScreenParams = nav.screenParams,
            )
        }
    }

    data class ProviderKey(
        val providerParams: IntentScreenParams<*>,
        val targetKey: ScreenKey,
    )
}
