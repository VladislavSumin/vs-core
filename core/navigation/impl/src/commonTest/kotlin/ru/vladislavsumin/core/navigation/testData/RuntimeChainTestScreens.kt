package ru.vladislavsumin.core.navigation.testData

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.host.childNavigationStack
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

/**
 * Тестовый харнесс для проверки поведения runtime-цепочек: когда открытие цепочки экранов создаёт новый
 * промежуточный экран, тот должен использовать default-лямбду (как при deep-link на старте), а не initial-лямбду.
 *
 * Граф: ChainRoot (Pages, HostA) -> StackMiddle(id) (Stack, HostB) / PagesMiddle(id) (Pages, HostB) -> Leaf(id).
 *
 * У промежуточных экранов default- и initial-лямбды намеренно различаются, чтобы тесты могли их различить.
 */

// region params

@Serializable
data object ChainRootParams : ScreenParams

@Serializable
data class StackMiddleParams(val id: Int) : ScreenParams

@Serializable
data class PagesMiddleParams(val id: Int) : ScreenParams

// endregion

// region screens

class StackMiddleScreen(
    context: ComponentContext,
    initialStack: List<IntentScreenParams<*>>,
    defaultStack: List<IntentScreenParams<*>>,
) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
        registerCustomFactory<IntentLeafParams, TestLeafIntent, IntentLeafScreen> { ctx, params, intents ->
            IntentLeafScreen(params, intents, ctx)
        }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostB,
        defaultStack = { defaultStack },
        initialStack = { initialStack },
    )

    @Composable
    override fun Render(modifier: Modifier) = Unit
}

class PagesMiddleScreen(
    context: ComponentContext,
    initialPages: Pages<IntentScreenParams<*>>,
    defaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>>,
) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
        registerCustomFactory<IntentLeafParams, TestLeafIntent, IntentLeafScreen> { ctx, params, intents ->
            IntentLeafScreen(params, intents, ctx)
        }
    }

    val pages: Value<ChildPages<ConfigurationHolder, Screen>> = childNavigationPages(
        navigationHost = NavigationHostB,
        pageStatus = { index, pagesState ->
            if (index == pagesState.selectedIndex) ChildNavState.Status.RESUMED else ChildNavState.Status.CREATED
        },
        initialPages = { initialPages },
        defaultPages = defaultPages,
    )

    @Composable
    override fun Render(modifier: Modifier) = Unit
}

class ChainRootScreen(
    context: ComponentContext,
    stackInitialStack: List<IntentScreenParams<*>>,
    stackDefaultStack: List<IntentScreenParams<*>>,
    pagesInitialPages: Pages<IntentScreenParams<*>>,
    pagesDefaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>>,
) : Screen(context) {
    init {
        registerCustomFactory<StackMiddleParams, NoIntent, StackMiddleScreen> { ctx, _, _ ->
            StackMiddleScreen(ctx, stackInitialStack, stackDefaultStack)
        }
        registerCustomFactory<PagesMiddleParams, NoIntent, PagesMiddleScreen> { ctx, _, _ ->
            PagesMiddleScreen(ctx, pagesInitialPages, pagesDefaultPages)
        }
    }

    val pages: Value<ChildPages<ConfigurationHolder, Screen>> = childNavigationPages(
        navigationHost = NavigationHostA,
        pageStatus = { index, pagesState ->
            if (index == pagesState.selectedIndex) ChildNavState.Status.RESUMED else ChildNavState.Status.CREATED
        },
        initialPages = { Pages(items = listOf(StackMiddleParams(0)), selectedIndex = 0) },
    )

    fun open(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun openWithIntent(screenParams: IntentLeafParams, intent: TestLeafIntent, hints: List<IntentScreenParams<*>>) {
        navigator.open(screenParams, intent = intent, hints = hints)
    }

    fun transfer(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>>) {
        navigator.transfer(screenParams, hints = hints)
    }

    @Composable
    override fun Render(modifier: Modifier) = Unit
}

// endregion

// region factory & navigation builder

class ChainRootFactory(
    private val stackInitialStack: List<IntentScreenParams<*>>,
    private val stackDefaultStack: List<IntentScreenParams<*>>,
    private val pagesInitialPages: Pages<IntentScreenParams<*>>,
    private val pagesDefaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>>,
) : ScreenFactory<ComponentContext, ChainRootParams, NoIntent, ChainRootScreen> {
    override fun create(
        context: ComponentContext,
        params: ChainRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): ChainRootScreen = ChainRootScreen(
        context = context,
        stackInitialStack = stackInitialStack,
        stackDefaultStack = stackDefaultStack,
        pagesInitialPages = pagesInitialPages,
        pagesDefaultPages = pagesDefaultPages,
    )
}

fun chainDefaultNavigation(
    stackInitialStack: List<IntentScreenParams<*>> = listOf(LeafParams(100)),
    stackDefaultStack: List<IntentScreenParams<*>> = listOf(LeafParams(200)),
    pagesInitialPages: Pages<IntentScreenParams<*>> = Pages(listOf(LeafParams(100)), 0),
    pagesDefaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>> =
        { params -> Pages(listOf(LeafParams(200), params), 1) },
): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = ChainRootParams,
                factory = ChainRootFactory(
                    stackInitialStack = stackInitialStack,
                    stackDefaultStack = stackDefaultStack,
                    pagesInitialPages = pagesInitialPages,
                    pagesDefaultPages = pagesDefaultPages,
                ),
                navigationHosts = {
                    NavigationHostA opens setOf(StackMiddleParams::class, PagesMiddleParams::class)
                },
            )
            registerScreen<StackMiddleParams>(
                defaultParams = StackMiddleParams(0),
                navigationHosts = { NavigationHostB opens setOf(LeafParams::class, IntentLeafParams::class) },
            )
            registerScreen<PagesMiddleParams>(
                defaultParams = PagesMiddleParams(0),
                navigationHosts = { NavigationHostB opens setOf(LeafParams::class, IntentLeafParams::class) },
            )
            registerScreen<LeafParams>()
            registerScreen<IntentLeafParams>()
        },
    ),
)

// endregion
