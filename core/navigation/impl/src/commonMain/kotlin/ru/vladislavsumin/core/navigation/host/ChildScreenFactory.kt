package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.statekeeper.SerializableContainer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenNavigatorHolder
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

/**
 * Стандартная фабрика дочерних экранов для использования в compose навигации.
 *
 * Все экраны строятся на управляемом контексте (TransferableScreenHolder),
 * что делает их универсально переносимыми (transferable) без дополнительных маркеров.
 */
@Suppress("UNCHECKED_CAST")
internal fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.childScreenFactory(
    configuration: ConfigurationHolder,
    childScreenContext: Ctx,
): GenericScreen<Ctx> {
    val screenParams = configuration.screenParams
    val stateKey = STATE_KEY

    val holder: TransferableScreenHolder<Ctx> = if (configuration.savedInstance != null) {
        // Усыновление: экран был перенесён из другой локации.
        // Создаём новый holder с живым instanceKeeper (со всеми VM) и сохранённым stateKeeper,
        // строим свежий экран на новом контексте — так window-зависимости инжектятся заново,
        // а viewModel выживает через общий instanceKeeper.
        val saved = configuration.savedInstance as TransferableScreenHolder<Ctx>
        val newHolder = TransferableScreenHolder<Ctx>(
            key = screenParams,
            savedState = saved.stateKeeper.save(),
            instanceKeeper = saved.instanceKeeper,
            restoredSaveable = saved.saveableStateRegistry.captureRaw(),
        )
        buildScreen(newHolder, childScreenContext, configuration)
        newHolder.bindTo(childScreenContext, stateKey)
        saved.destroyWithoutInstanceKeeper()
        newHolder
    } else {
        // Нормальное создание или восстановление после смены конфигурации.
        childScreenContext.instanceKeeper.getOrCreate<TransferableScreenHolder<Ctx>>(screenParams) {
            val savedState = childScreenContext.stateKeeper.consume(
                stateKey, SerializableContainer.serializer(),
            )
            val h = TransferableScreenHolder<Ctx>(key = screenParams, savedState = savedState)
            buildScreen(h, childScreenContext, configuration)
            h
        }.also { h ->
            h.bindTo(childScreenContext, stateKey)
        }
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

private const val STATE_KEY = "saved_screen_holder"
