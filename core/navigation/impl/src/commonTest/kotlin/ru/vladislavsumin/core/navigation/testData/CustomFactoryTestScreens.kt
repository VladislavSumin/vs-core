package ru.vladislavsumin.core.navigation.testData

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.host.childNavigationSlot
import ru.vladislavsumin.core.navigation.host.childNavigationStack
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import kotlinx.coroutines.channels.ReceiveChannel

@Serializable
data object CrossRootParams : ScreenParams

@Serializable
data class CrossMiddleParams(val id: Int = 0) : ScreenParams

class CrossRootScreen(context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<CrossMiddleParams, NoIntent, CrossMiddleScreen> { ctx, params, _ ->
            CrossMiddleScreen(params, ctx)
        }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostA,
        initialStack = { listOf(CrossMiddleParams(0)) },
    )

    fun middle(index: Int): CrossMiddleScreen =
        stack.value.items[index].instance as CrossMiddleScreen

    fun open(params: CrossMiddleParams) = navigator.open(params)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class CrossMiddleScreen(
    val params: CrossMiddleParams,
    context: ComponentContext,
) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, leafParams, _ ->
            LeafScreen(leafParams, ctx)
        }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostB,
        initialStack = { listOf(LeafParams(0)) },
    )

    fun openLeaf(params: LeafParams) = navigator.open(params)

    fun openLeafWithCustomFactory(params: LeafParams) = navigator.openWithCustomFactory(params)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class CrossRootFactory : ScreenFactory<ComponentContext, CrossRootParams, NoIntent, CrossRootScreen> {
    override fun create(
        context: ComponentContext,
        params: CrossRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): CrossRootScreen = CrossRootScreen(context)
}

fun crossHostNavigation(): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = CrossRootParams,
                factory = CrossRootFactory(),
                navigationHosts = { NavigationHostA opens setOf(CrossMiddleParams::class) },
            )
            registerScreen<CrossMiddleParams>(
                navigationHosts = { NavigationHostB opens setOf(LeafParams::class) },
            )
            registerScreen<LeafParams>()
        },
    ),
)

@Serializable
data object ProviderRootParams : ScreenParams

@Serializable
data object ProviderParams : ScreenParams

@Serializable
data class TargetParams(val id: Int) : ScreenParams

class ProviderRootScreen(context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<ProviderParams, NoIntent, ProviderScreen> { ctx, params, _ ->
            ProviderScreen(params, ctx)
        }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostA,
        initialStack = { listOf(ProviderParams) },
    )

    fun provider(): ProviderScreen = stack.value.items.first().instance as ProviderScreen

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class ProviderScreen(
    val params: ProviderParams,
    context: ComponentContext,
) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    var createdTarget: TargetScreen? = null
        private set

    init {
        registerCustomFactory<TargetParams, NoIntent, TargetScreen> { ctx, targetParams, _ ->
            TargetScreen(targetParams, ctx).also { createdTarget = it }
        }
    }

    val slot: Value<ChildSlot<ConfigurationHolder, Screen>> = childNavigationSlot(
        navigationHost = NavigationHostB,
        initialConfiguration = { null },
    )

    fun openTargetWithCustomFactory(params: TargetParams) {
        navigator.openWithCustomFactory(params)
    }

    fun openTargetNormal(params: TargetParams) {
        navigator.open(params)
    }

    fun closeTarget(params: TargetParams) = navigator.close(params)

    fun targetSlotChild(): Screen? = slot.value.child?.instance

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class TargetScreen(
    val params: TargetParams,
    context: ComponentContext,
) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class ProviderRootFactory : ScreenFactory<ComponentContext, ProviderRootParams, NoIntent, ProviderRootScreen> {
    override fun create(
        context: ComponentContext,
        params: ProviderRootParams,
        intents: kotlinx.coroutines.channels.ReceiveChannel<NoIntent>,
    ): ProviderRootScreen = ProviderRootScreen(context)
}

fun providerCustomFactoryNavigation(): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = ProviderRootParams,
                factory = ProviderRootFactory(),
                navigationHosts = { NavigationHostA opens setOf(ProviderParams::class) },
            )
            registerScreen<ProviderParams>(
                navigationHosts = { NavigationHostB opens setOf(TargetParams::class) },
            )
            registerScreen<TargetParams>()
        },
    ),
)
