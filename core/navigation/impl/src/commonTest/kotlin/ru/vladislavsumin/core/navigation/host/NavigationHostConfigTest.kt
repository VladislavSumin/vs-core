package ru.vladislavsumin.core.navigation.host

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.decompose.test.TestComponentContext
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.testData.CountingViewModel
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.SlotRootScreen
import ru.vladislavsumin.core.navigation.testData.StackRootParams
import ru.vladislavsumin.core.navigation.testData.paramsList
import ru.vladislavsumin.core.navigation.testData.slotNavigation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Характеризационные тесты параметров конфигурации navigation-хостов.
 */
class NavigationHostConfigTest {

    @Test
    fun allowStateSaveFalseDiscardsSavedState() = runTest {
        setMain()
        LoggerManager.initTest()
        val navigation = noStateSaveStackNav()
        val context = TestComponentContext()

        val root1 = context.childNavigationRoot(navigation) as NoStateSaveStackScreen
        context.lifecycleRegistry.resume()
        root1.open(LeafParams(1))

        val savedState = context.stateKeeperDispatcher.save()
        context.instanceKeeperDispatcher.destroy()
        context.lifecycleRegistry.destroy()
        val context2 = TestComponentContext(
            stateKeeperDispatcher = StateKeeperDispatcher(savedState),
            instanceKeeperDispatcher = InstanceKeeperDispatcher(),
        )
        val root2 = context2.childNavigationRoot(navigation) as NoStateSaveStackScreen
        context2.lifecycleRegistry.resume()

        assertEquals(listOf(LeafParams(0)), root2.stack.value.paramsList)
    }

    @Test
    fun allowStateSaveFalsePagesDiscardsState() = runTest {
        setMain()
        LoggerManager.initTest()
        val navigation = noStateSavePagesNav()
        val context = TestComponentContext()

        val root1 = context.childNavigationRoot(navigation) as NoStateSavePagesScreen
        context.lifecycleRegistry.resume()
        root1.open(LeafParams(1))

        val savedState = context.stateKeeperDispatcher.save()
        context.instanceKeeperDispatcher.destroy()
        context.lifecycleRegistry.destroy()
        val context2 = TestComponentContext(
            stateKeeperDispatcher = StateKeeperDispatcher(savedState),
            instanceKeeperDispatcher = InstanceKeeperDispatcher(),
        )
        val root2 = context2.childNavigationRoot(navigation) as NoStateSavePagesScreen
        context2.lifecycleRegistry.resume()

        assertEquals(listOf(LeafParams(0)), root2.pages.value.paramsList)
    }

    @Test
    fun slotStateSurvivesConfigurationChange() = runTest {
        setMain()
        LoggerManager.initTest()
        CountingViewModel.nextId = 0
        val navigation = slotNavigation(LeafParams(0))
        val context = TestComponentContext()

        val root1 = context.childNavigationRoot(navigation) as SlotRootScreen
        context.lifecycleRegistry.resume()
        assertEquals(LeafParams(0), root1.slot.value.child?.configuration?.screenParams)

        val savedState = context.stateKeeperDispatcher.save()
        val instanceKeeper = context.instanceKeeperDispatcher
        context.lifecycleRegistry.destroy()
        val context2 = TestComponentContext(
            stateKeeperDispatcher = StateKeeperDispatcher(savedState),
            instanceKeeperDispatcher = instanceKeeper,
        )
        val root2 = context2.childNavigationRoot(navigation) as SlotRootScreen
        context2.lifecycleRegistry.resume()

        assertNotNull(root2.slot.value.child)
        assertEquals(LeafParams(0), root2.slot.value.child?.configuration?.screenParams)
    }

    companion object {
        private fun noStateSaveStackNav(): Navigation = Navigation(
            setOf(
                GenericNavigationRegistrar {
                    registerScreen(
                        defaultParams = StackRootParams,
                        factory = NoStateSaveStackScreenFactory(),
                        navigationHosts = {
                            NavigationHostA opens setOf(LeafParams::class)
                        },
                    )
                    registerScreen<LeafParams>()
                },
            ),
        )

        private fun noStateSavePagesNav(): Navigation = Navigation(
            setOf(
                GenericNavigationRegistrar {
                    registerScreen(
                        defaultParams = NoStateSavePagesRootParams,
                        factory = NoStateSavePagesScreenFactory(),
                        navigationHosts = {
                            NavigationHostA opens setOf(LeafParams::class)
                        },
                    )
                    registerScreen<LeafParams>()
                },
            ),
        )
    }
}

@Serializable
private data object NoStateSavePagesRootParams : ScreenParams

private class NoStateSaveStackScreen(context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostA,
        initialStack = { listOf(LeafParams(0)) },
        allowStateSave = false,
    )

    fun open(screenParams: IntentScreenParams<*>) {
        navigator.open(screenParams)
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

@Suppress("UNCHECKED_CAST")
private class NoStateSaveStackScreenFactory :
    ScreenFactory<ComponentContext, StackRootParams, NoIntent, NoStateSaveStackScreen> {
    override fun create(
        context: ComponentContext,
        params: StackRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): NoStateSaveStackScreen = NoStateSaveStackScreen(context)
}

private class NoStateSavePagesScreen(context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
    }

    private val pagesResult = childNavigationPages(
        navigationHost = NavigationHostA,
        pageStatus = { index, pagesState ->
            if (index == pagesState.selectedIndex) ChildNavState.Status.RESUMED else ChildNavState.Status.CREATED
        },
        initialPages = { Pages(listOf(LeafParams(0)), 0) },
        allowStateSave = false,
    )
    val pages = pagesResult.pages

    fun open(screenParams: IntentScreenParams<*>) {
        navigator.open(screenParams)
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

@Suppress("UNCHECKED_CAST")
private class NoStateSavePagesScreenFactory :
    ScreenFactory<ComponentContext, NoStateSavePagesRootParams, NoIntent, NoStateSavePagesScreen> {
    override fun create(
        context: ComponentContext,
        params: NoStateSavePagesRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): NoStateSavePagesScreen = NoStateSavePagesScreen(context)
}
