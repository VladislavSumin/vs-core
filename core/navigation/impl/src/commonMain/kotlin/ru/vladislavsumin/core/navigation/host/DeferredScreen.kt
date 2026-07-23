package ru.vladislavsumin.core.navigation.host

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.FactoryProviderRegistry
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.ScreenNavigatorHolder
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

internal class DeferredScreen<Ctx : GenericComponentContext<Ctx>>(
    context: Ctx,
    private val holder: TransferableScreenHolder<Ctx>,
    private val configuration: ConfigurationHolder,
    providerParams: IntentScreenParams<*>,
    targetKey: ScreenKey,
    registry: FactoryProviderRegistry<Ctx>,
) : GenericScreen<Ctx>(context) {

    private var realScreen: GenericScreen<Ctx>? by mutableStateOf(null)

    init {
        registry.subscribe(providerParams, targetKey) { factory ->
            createRealScreen(factory)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createRealScreen(factory: ScreenFactory<Ctx, *, *, *>) {
        val screen = try {
            ScreenNavigatorHolder = internalNavigator
            (factory as ScreenFactory<Ctx, IntentScreenParams<ScreenIntent>, ScreenIntent, GenericScreen<Ctx>>)
                .create(
                    context = context,
                    params = configuration.screenParams as IntentScreenParams<ScreenIntent>,
                    intents = configuration.intentReceiveChannel,
                )
        } finally {
            ScreenNavigatorHolder = null
        }
        internalNavigator.screen = screen
        holder.screen = screen
        realScreen = screen
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) {
        realScreen?.Render(modifier)
    }
}
