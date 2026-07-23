package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.statekeeper.SerializableContainer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenNavigatorHolder
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

@Suppress("UNCHECKED_CAST")
internal fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.childScreenFactory(
    configuration: ConfigurationHolder,
    childScreenContext: Ctx,
): GenericScreen<Ctx> {
    val screenParams = configuration.screenParams
    val screenKey = screenParams.asKey()
    val providerParams = configuration.providerParams

    val factory = internalNavigator.tryGetChildScreenFactory(screenKey)
    val needsDeferred = factory == null && providerParams != null

    if (needsDeferred) {
        val effectiveProviderParams = providerParams
            ?: (configuration.savedInstance as? TransferableScreenHolder<Ctx>)?.providerParams
        val holder: TransferableScreenHolder<Ctx> = if (configuration.savedInstance != null) {
            val saved = configuration.savedInstance as TransferableScreenHolder<Ctx>
            val newHolder = TransferableScreenHolder<Ctx>(
                savedState = saved.stateKeeper.save(),
                instanceKeeper = saved.instanceKeeper,
                restoredSaveable = saved.saveableStateRegistry.captureRaw(),
                providerParams = saved.providerParams,
            )
            saved.destroyWithoutInstanceKeeper()
            newHolder
        } else {
            val savedState = childScreenContext.stateKeeper.consume(
                STATE_KEY,
                SerializableContainer.serializer(),
            )
            val instanceKeeper = childScreenContext.instanceKeeper.get(
                STATE_KEY,
            ) as? TransferableScreenHolder.InstanceKeeperHolder
            TransferableScreenHolder<Ctx>(
                savedState = savedState,
                instanceKeeper = instanceKeeper?.dispatcher,
                providerParams = effectiveProviderParams,
            )
        }

        val holderContext = holder.createContext(childScreenContext.componentContextFactory)
        val childNavigator = internalNavigator.createChildNavigator(screenParams, holderContext)
        childNavigator.holder = holder
        holder.navigator = childNavigator
        holder.bindTo(childScreenContext, STATE_KEY)
        providerParams?.let { pp ->
            internalNavigator.globalNavigator.factoryProviderRegistry.registerDependent(pp, childNavigator)
        }

        val deferredScreen = try {
            ScreenNavigatorHolder = childNavigator
            DeferredScreen(
                context = holderContext,
                holder = holder,
                configuration = configuration,
                providerParams = providerParams!!,
                targetKey = screenKey,
                registry = internalNavigator.globalNavigator.factoryProviderRegistry,
            )
        } finally {
            ScreenNavigatorHolder = null
        }
        childNavigator.screen = deferredScreen
        holder.screen = deferredScreen
        return deferredScreen
    }

    val holder: TransferableScreenHolder<Ctx> = if (configuration.savedInstance != null) {
        val saved = configuration.savedInstance as TransferableScreenHolder<Ctx>
        val newHolder = TransferableScreenHolder<Ctx>(
            savedState = saved.stateKeeper.save(),
            instanceKeeper = saved.instanceKeeper,
            restoredSaveable = saved.saveableStateRegistry.captureRaw(),
            providerParams = saved.providerParams,
        )
        buildScreen(newHolder, childScreenContext, configuration)
        newHolder.bindTo(childScreenContext, STATE_KEY)
        providerParams?.let { pp ->
            internalNavigator.globalNavigator.factoryProviderRegistry.registerDependent(pp, newHolder.navigator)
        }
        saved.destroyWithoutInstanceKeeper()
        newHolder
    } else {
        val savedState = childScreenContext.stateKeeper.consume(
            STATE_KEY,
            SerializableContainer.serializer(),
        )
        val instanceKeeper = childScreenContext.instanceKeeper.get(
            STATE_KEY,
        ) as? TransferableScreenHolder.InstanceKeeperHolder
        val holder = TransferableScreenHolder<Ctx>(
            savedState = savedState,
            instanceKeeper = instanceKeeper?.dispatcher,
            providerParams = providerParams,
        )
        buildScreen(holder, childScreenContext, configuration)
        holder.bindTo(childScreenContext, STATE_KEY)
        providerParams?.let { pp ->
            internalNavigator.globalNavigator.factoryProviderRegistry.registerDependent(pp, holder.navigator)
        }
        holder
    }

    return holder.screen
}

private fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.buildScreen(
    holder: TransferableScreenHolder<Ctx>,
    childScreenContext: Ctx,
    configuration: ConfigurationHolder,
) {
    val screenParams = configuration.screenParams
    val holderContext = holder.createContext(childScreenContext.componentContextFactory)
    val childNavigator = internalNavigator.createChildNavigator(screenParams, holderContext)
    childNavigator.holder = holder
    holder.navigator = childNavigator

    val screenFactory = internalNavigator.getChildScreenFactory(screenParams.asKey())
    val screen = try {
        ScreenNavigatorHolder = childNavigator
        screenFactory.create(
            context = holderContext,
            params = screenParams as IntentScreenParams<ScreenIntent>,
            intents = configuration.intentReceiveChannel,
        )
    } finally {
        ScreenNavigatorHolder = null
    }
    childNavigator.screen = screen
    holder.screen = screen
}

private const val STATE_KEY = "ru.vladislavsumin.core.navigation.host:saved_screen_holder"
