package ru.vladislavsumin.core.navigation.testData

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.host.childNavigationPages
import ru.vladislavsumin.core.navigation.host.childNavigationSlot
import ru.vladislavsumin.core.navigation.host.childNavigationStack
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

/**
 * Тестовые экраны и view model'и для end-to-end характеризационных тестов навигации.
 *
 * Задача этого харнесса — закрепить текущее наблюдаемое поведение навигации (Pages/Stack/Slot,
 * childScreenFactory, ScreenNavigator, удержание viewModel) перед рефакторингом под transfer.
 */

// region params

@Serializable
data class LeafParams(val id: Int) : ScreenParams

/**
 * Лист с поддержкой intent'ов, чтобы проверять доставку intent через [ConfigurationHolder].
 */
@Serializable
data class IntentLeafParams(val id: Int) : IntentScreenParams<TestLeafIntent>

@Serializable
data class TestLeafIntent(val payload: Int) : ScreenIntent

@Serializable
data object PagesRootParams : ScreenParams

@Serializable
data object StackRootParams : ScreenParams

@Serializable
data object SlotRootParams : ScreenParams

@Serializable
data object NestedRootParams : ScreenParams

@Serializable
data class MiddleParams(val id: Int) : ScreenParams

@Serializable
data object OuterSlotParams : ScreenParams

@Serializable
data class InnerStackParams(val closeParentWhenEmpty: Boolean) : ScreenParams

@Serializable
data class InnerPagesParams(val closeParentWhenEmpty: Boolean) : ScreenParams

// endregion

// region view model

/**
 * ViewModel, по которой удобно проверять удержание/уничтожение:
 * - [counter] уникален для каждого нового инстанса (переживает config change как тот же инстанс, а
 *   process death — как восстановленное значение);
 * - [isActive] показывает, отменён ли [viewModelScope] (т.е. была ли вызвана onDestroy).
 */
class CountingViewModel : ViewModel() {
    val counter = saveableStateFlow("counter") { nextId++ }
    val isActive: Boolean get() = viewModelScope.isActive

    /** Текущее сохраняемое значение (переживает config change и process death). */
    val value: Int get() = counter.value

    fun update(newValue: Int) {
        counter.value = newValue
    }

    companion object {
        var nextId: Int = 0
    }
}

// endregion

// region screens

class LeafScreen(val params: LeafParams, context: ComponentContext) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    fun openScreen(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun closeScreen(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    fun transferScreen(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>>) {
        navigator.transfer(screenParams, hints = hints)
    }

    fun closeSelf() = navigator.close()

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class IntentLeafScreen(
    val params: IntentLeafParams,
    intents: ReceiveChannel<TestLeafIntent>,
    context: ComponentContext,
) : GenericScreen<ComponentContext>(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }
    val receivedIntents: MutableList<TestLeafIntent> = mutableListOf()

    init {
        launch {
            for (intent in intents) receivedIntents += intent
        }
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

/**
 * Корневой экран с host'ом типа Pages. По умолчанию неактивные страницы держатся в CREATED
 * (как в реальном RootScreen приложения), чтобы можно было проверять удержание состояния табов.
 */
class PagesRootScreen(
    context: ComponentContext,
    keepInactive: Boolean,
    initial: List<IntentScreenParams<*>>,
    selectedIndex: Int,
    defaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>> =
        { params -> Pages(listOf(params), 0) },
) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
        registerCustomFactory<IntentLeafParams, TestLeafIntent, IntentLeafScreen> { ctx, params, intents ->
            IntentLeafScreen(params, intents, ctx)
        }
    }

    private val pagesResult = childNavigationPages(
        navigationHost = NavigationHostA,
        pageStatus = { index, pagesState ->
            when {
                index == pagesState.selectedIndex -> ChildNavState.Status.RESUMED
                keepInactive -> ChildNavState.Status.CREATED
                else -> ChildNavState.Status.DESTROYED
            }
        },
        initialPages = { Pages(items = initial, selectedIndex = selectedIndex) },
        defaultPages = defaultPages,
    )
    val pages = pagesResult.pages
    val pagesController = pagesResult.controller

    fun open(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun openWithIntent(screenParams: IntentLeafParams, intent: TestLeafIntent) {
        navigator.open(screenParams, intent = intent)
    }

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    fun transfer(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>>) {
        navigator.transfer(screenParams, hints = hints)
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class StackRootScreen(
    context: ComponentContext,
    private val defaultStack: List<IntentScreenParams<*>> = emptyList(),
    initial: List<IntentScreenParams<*>>,
) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostA,
        defaultStack = { defaultStack },
        initialStack = { initial },
    )

    fun open(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun openWithIntent(screenParams: IntentLeafParams, intent: TestLeafIntent) {
        navigator.open(screenParams, intent = intent)
    }

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class SlotRootScreen(context: ComponentContext, initial: IntentScreenParams<*>?) : Screen(context) {
    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
    }

    val slot: Value<ChildSlot<ConfigurationHolder, Screen>> = childNavigationSlot(
        navigationHost = NavigationHostA,
        initialConfiguration = { initial },
    )

    fun open(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun openWithIntent(screenParams: IntentLeafParams, intent: TestLeafIntent) {
        navigator.open(screenParams, intent = intent)
    }

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

/**
 * Экран второго уровня со стековым host'ом. Используется для проверки вложенной навигации, корректности
 * [ru.vladislavsumin.core.navigation.screen.ScreenPath] и очистки регистраций дочерних навигаторов.
 */
class MiddleScreen(val params: MiddleParams, context: ComponentContext) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, leafParams, _ -> LeafScreen(leafParams, ctx) }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostB,
        initialStack = { listOf(LeafParams(0)) },
    )

    fun open(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    fun transfer(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>>) {
        navigator.transfer(screenParams, hints = hints)
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

class NestedRootScreen(context: ComponentContext) : Screen(context) {
    init {
        registerCustomFactory<MiddleParams, NoIntent, MiddleScreen> { ctx, params, _ -> MiddleScreen(params, ctx) }
    }

    private val pagesResult = childNavigationPages(
        navigationHost = NavigationHostA,
        pageStatus = { index, pagesState ->
            if (index == pagesState.selectedIndex) ChildNavState.Status.RESUMED else ChildNavState.Status.CREATED
        },
        initialPages = { Pages(items = listOf(MiddleParams(0)), selectedIndex = 0) },
    )
    val pages = pagesResult.pages

    fun open(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>> = emptyList()) {
        navigator.open(screenParams, hints = hints)
    }

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    fun transfer(screenParams: IntentScreenParams<*>, hints: List<IntentScreenParams<*>>) {
        navigator.transfer(screenParams, hints = hints)
    }

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

// endregion

// region close-parent-when-empty screens

/**
 * Внешний экран со слот-host'ом, в который помещается вложенный стек/pages экран. Используется для проверки
 * закрытия родительского экрана через `closeParentWhenEmpty`: когда вложенный экран закрывает сам себя, слот
 * очищается (child становится null).
 */
class OuterSlotScreen(context: ComponentContext, initial: IntentScreenParams<*>) : Screen(context) {
    init {
        registerCustomFactory<InnerStackParams, NoIntent, InnerStackScreen> { ctx, params, _ ->
            InnerStackScreen(ctx, params.closeParentWhenEmpty)
        }
        registerCustomFactory<InnerPagesParams, NoIntent, InnerPagesScreen> { ctx, params, _ ->
            InnerPagesScreen(ctx, params.closeParentWhenEmpty)
        }
    }

    val slot: Value<ChildSlot<ConfigurationHolder, Screen>> = childNavigationSlot(
        navigationHost = NavigationHostA,
        initialConfiguration = { initial },
    )

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

/**
 * Вложенный экран со стек-host'ом, поддерживающий `closeParentWhenEmpty`.
 */
class InnerStackScreen(context: ComponentContext, closeParentWhenEmpty: Boolean) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
    }

    val stack: Value<ChildStack<ConfigurationHolder, Screen>> = childNavigationStack(
        navigationHost = NavigationHostB,
        initialStack = { listOf(LeafParams(0)) },
        closeParentWhenEmpty = closeParentWhenEmpty,
    )

    fun open(screenParams: IntentScreenParams<*>) = navigator.open(screenParams)

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

/**
 * Вложенный экран с pages-host'ом, поддерживающий `closeParentWhenEmpty`.
 */
class InnerPagesScreen(context: ComponentContext, closeParentWhenEmpty: Boolean) : Screen(context) {
    val vm: CountingViewModel = viewModel { CountingViewModel() }

    init {
        registerCustomFactory<LeafParams, NoIntent, LeafScreen> { ctx, params, _ -> LeafScreen(params, ctx) }
    }

    private val pagesResult = childNavigationPages(
        navigationHost = NavigationHostB,
        pageStatus = { index, pagesState ->
            if (index == pagesState.selectedIndex) ChildNavState.Status.RESUMED else ChildNavState.Status.CREATED
        },
        initialPages = { Pages(items = listOf(LeafParams(0)), selectedIndex = 0) },
        closeParentWhenEmpty = closeParentWhenEmpty,
    )
    val pages = pagesResult.pages

    fun open(screenParams: IntentScreenParams<*>) = navigator.open(screenParams)

    fun close(screenParams: IntentScreenParams<*>) = navigator.close(screenParams)

    @Composable
    override fun RenderScreen(modifier: Modifier) = Unit
}

// endregion

// region factories & navigation builders

class PagesRootFactory(
    private val keepInactive: Boolean,
    private val initial: List<IntentScreenParams<*>>,
    private val selectedIndex: Int,
    private val defaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>> =
        { params -> Pages(listOf(params), 0) },
) : ScreenFactory<ComponentContext, PagesRootParams, NoIntent, PagesRootScreen> {
    override fun create(
        context: ComponentContext,
        params: PagesRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): PagesRootScreen = PagesRootScreen(context, keepInactive, initial, selectedIndex, defaultPages)
}

class StackRootFactory(
    private val initial: List<IntentScreenParams<*>>,
    private val defaultStack: List<IntentScreenParams<*>> = emptyList(),
) : ScreenFactory<ComponentContext, StackRootParams, NoIntent, StackRootScreen> {
    override fun create(
        context: ComponentContext,
        params: StackRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): StackRootScreen = StackRootScreen(context, defaultStack, initial)
}

class SlotRootFactory(private val initial: IntentScreenParams<*>?) :
    ScreenFactory<ComponentContext, SlotRootParams, NoIntent, SlotRootScreen> {
    override fun create(
        context: ComponentContext,
        params: SlotRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): SlotRootScreen = SlotRootScreen(context, initial)
}

class NestedRootFactory : ScreenFactory<ComponentContext, NestedRootParams, NoIntent, NestedRootScreen> {
    override fun create(
        context: ComponentContext,
        params: NestedRootParams,
        intents: ReceiveChannel<NoIntent>,
    ): NestedRootScreen = NestedRootScreen(context)
}

class OuterSlotFactory(private val initial: IntentScreenParams<*>) :
    ScreenFactory<ComponentContext, OuterSlotParams, NoIntent, OuterSlotScreen> {
    override fun create(
        context: ComponentContext,
        params: OuterSlotParams,
        intents: ReceiveChannel<NoIntent>,
    ): OuterSlotScreen = OuterSlotScreen(context, initial)
}

fun pagesNavigation(
    keepInactive: Boolean = true,
    initial: List<IntentScreenParams<*>> = listOf(LeafParams(0)),
    selectedIndex: Int = 0,
    defaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>> =
        { params -> Pages(listOf(params), 0) },
): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = PagesRootParams,
                factory = PagesRootFactory(keepInactive, initial, selectedIndex, defaultPages),
                navigationHosts = { NavigationHostA opens setOf(LeafParams::class, IntentLeafParams::class) },
            )
            registerScreen<LeafParams>()
            registerScreen<IntentLeafParams>()
        },
    ),
)

fun stackNavigation(initial: List<IntentScreenParams<*>> = listOf(LeafParams(0))): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = StackRootParams,
                factory = StackRootFactory(initial),
                navigationHosts = { NavigationHostA opens setOf(LeafParams::class) },
            )
            registerScreen<LeafParams>()
        },
    ),
)

fun slotNavigation(initial: IntentScreenParams<*>? = LeafParams(0)): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = SlotRootParams,
                factory = SlotRootFactory(initial),
                navigationHosts = { NavigationHostA opens setOf(LeafParams::class) },
            )
            registerScreen<LeafParams>()
        },
    ),
)

fun nestedNavigation(): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = NestedRootParams,
                factory = NestedRootFactory(),
                navigationHosts = { NavigationHostA opens setOf(MiddleParams::class) },
            )
            registerScreen<MiddleParams>(
                navigationHosts = { NavigationHostB opens setOf(LeafParams::class) },
            )
            registerScreen<LeafParams>()
        },
    ),
)

/**
 * Граф для проверки `closeParentWhenEmpty`: OuterSlot (Slot, HostA) -> Inner(Stack|Pages) (HostB) -> Leaf.
 * Внешний слот не закрывает родителя сам, поэтому при закрытии вложенным экраном самого себя слот просто опустеет.
 */
fun closeParentNavigation(inner: IntentScreenParams<*>): Navigation = Navigation(
    setOf(
        GenericNavigationRegistrar {
            registerScreen(
                defaultParams = OuterSlotParams,
                factory = OuterSlotFactory(inner),
                navigationHosts = {
                    NavigationHostA opens setOf(InnerStackParams::class, InnerPagesParams::class)
                },
            )
            registerScreen<InnerStackParams>(
                navigationHosts = { NavigationHostB opens setOf(LeafParams::class) },
            )
            registerScreen<InnerPagesParams>(
                navigationHosts = { NavigationHostB opens setOf(LeafParams::class) },
            )
            registerScreen<LeafParams>()
        },
    ),
)

// endregion

// region access helpers

fun ChildPages<ConfigurationHolder, Screen>.leaf(index: Int): LeafScreen = items[index].instance as LeafScreen

val ChildPages<ConfigurationHolder, Screen>.paramsList: List<IntentScreenParams<*>>
    get() = items.map { it.configuration.screenParams }

val ChildStack<ConfigurationHolder, Screen>.paramsList: List<IntentScreenParams<*>>
    get() = items.map { it.configuration.screenParams }

/** Текущее состояние lifecycle экрана (для проверки статусов активных/неактивных дочерних экранов). */
val GenericScreen<ComponentContext>.lifecycleState: Lifecycle.State
    get() = internalContext.lifecycle.state

// endregion
