package ru.vladislavsumin.core.navigation.host

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.decompose.test.TestComponentContext
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.StackRootParams
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals

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
    }
}

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
    override fun Render(modifier: Modifier) = Unit
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
