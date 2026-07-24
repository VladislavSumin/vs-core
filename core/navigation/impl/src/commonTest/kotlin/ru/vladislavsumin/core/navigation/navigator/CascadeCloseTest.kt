package ru.vladislavsumin.core.navigation.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.host.childNavigationRoot
import ru.vladislavsumin.core.navigation.host.childNavigationSlot
import ru.vladislavsumin.core.navigation.host.childNavigationStack
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.NavigationHostB
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Cascade close: при закрытии провайдера все зависимые экраны (открытые через openWithCustomFactory)
 * автоматически закрываются.
 *
 * Граф: Root (Slot, HostA) → Provider (Stack, HostB) → Target
 * Root (Slot) легко закрывает одного ребенка и открывает другого.
 */

@Serializable
data object CascadeRootParams : ScreenParams

@Serializable
data class CascadeProviderParams(val id: Int = 0) : ScreenParams

@Serializable
data class CascadeTargetParams(val id: Int) : ScreenParams

class CascadeRootScreen(context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<CascadeProviderParams, NoIntent, CascadeProviderScreen> { ctx, params, _ ->
            CascadeProviderScreen(params, ctx)
        }
    }

    val slot: Value<ChildSlot<ConfigurationHolder, Screen>> = childNavigationSlot(
        navigationHost = NavigationHostA,
        initialConfiguration = { CascadeProviderParams(0) },
    )

    fun provider(): CascadeProviderScreen? = slot.value.child?.instance as? CascadeProviderScreen

    fun openProvider(params: CascadeProviderParams) = navigator.open(params)

    fun closeProvider(params: CascadeProviderParams) = navigator.close(params)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class CascadeProviderScreen(val params: CascadeProviderParams, context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<CascadeTargetParams, NoIntent, CascadeTargetScreen> { ctx, tp, _ ->
            CascadeTargetScreen(tp, ctx)
        }
    }

    val stack: Value<com.arkivanov.decompose.router.stack.ChildStack<ConfigurationHolder, Screen>> =
        childNavigationStack(
            navigationHost = NavigationHostB,
            initialStack = { listOf(CascadeTargetParams(0)) },
        )

    fun openTarget(params: CascadeTargetParams) = navigator.openWithCustomFactory(params)

    fun targetStackParams(): List<IntentScreenParams<*>> = stack.value.items.map { it.configuration.screenParams }

    fun closeItself() = navigator.close()

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class CascadeTargetScreen(val params: CascadeTargetParams, context: ComponentContext) : Screen(context) {
    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class CascadeRootFactory : ScreenFactory<ComponentContext, CascadeRootParams, NoIntent, CascadeRootScreen> {
    override fun create(
        context: ComponentContext,
        params: CascadeRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): CascadeRootScreen = CascadeRootScreen(context)
}

class CascadeCustomFactoryTest : NavigationIntegrationTestBase() {

    private fun mount(): CascadeRootScreen {
        val nav = Navigation(
            setOf(
                GenericNavigationRegistrar {
                    registerScreen(
                        defaultParams = CascadeRootParams,
                        factory = CascadeRootFactory(),
                        navigationHosts = { NavigationHostA opens setOf(CascadeProviderParams::class) },
                    )
                    registerScreen<CascadeProviderParams>(
                        navigationHosts = { NavigationHostB opens setOf(CascadeTargetParams::class) },
                    )
                    registerScreen<CascadeTargetParams>()
                },
            ),
        )

        @Suppress("UNCHECKED_CAST")
        val root = context.childNavigationRoot(nav)
        lifecycle.resume()
        return root as CascadeRootScreen
    }

    @Test
    fun closingProviderClosesDependentTargets() = runTest {
        setMain()
        val root = mount()
        val provider0 = root.provider()!!
        assertNotNull(provider0)
        assertEquals(CascadeProviderParams(0), provider0.params)

        provider0.openTarget(CascadeTargetParams(1))
        provider0.openTarget(CascadeTargetParams(2))
        assertEquals(
            listOf(CascadeTargetParams(0), CascadeTargetParams(1), CascadeTargetParams(2)),
            provider0.targetStackParams(),
        )

        // Open another Provider in the slot (replaces slot, closes Provider(0))
        root.openProvider(CascadeProviderParams(1))

        val provider1 = root.provider()!!
        assertEquals(
            CascadeProviderParams(1),
            provider1.params,
            "Slot must now contain Provider(1)",
        )

        // Provider(0) is destroyed — its targets should be cascade-closed
        // Provider(1) starts fresh
        assertEquals(
            listOf(CascadeTargetParams(0)),
            provider1.targetStackParams(),
        )
    }

    @Test
    fun providerCloseItselfDestroysTargets() = runTest {
        setMain()
        val root = mount()
        val provider = root.provider()!!

        provider.openTarget(CascadeTargetParams(1))
        assertEquals(2, provider.targetStackParams().size)

        provider.closeItself()

        assertNull(root.provider(), "Slot must be empty after Provider closed itself")
    }
}
