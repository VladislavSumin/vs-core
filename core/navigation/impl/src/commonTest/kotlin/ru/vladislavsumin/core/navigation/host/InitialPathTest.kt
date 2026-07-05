package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.Pages
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.testData.IntentLeafParams
import ru.vladislavsumin.core.navigation.testData.IntentLeafScreen
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.MiddleParams
import ru.vladislavsumin.core.navigation.testData.MiddleScreen
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.NavigationHostB
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.NestedRootFactory
import ru.vladislavsumin.core.navigation.testData.NestedRootParams
import ru.vladislavsumin.core.navigation.testData.NestedRootScreen
import ru.vladislavsumin.core.navigation.testData.PagesRootScreen
import ru.vladislavsumin.core.navigation.testData.SlotRootScreen
import ru.vladislavsumin.core.navigation.testData.StackRootFactory
import ru.vladislavsumin.core.navigation.testData.StackRootParams
import ru.vladislavsumin.core.navigation.testData.StackRootScreen
import ru.vladislavsumin.core.navigation.testData.TestLeafIntent
import ru.vladislavsumin.core.navigation.testData.pagesNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import ru.vladislavsumin.core.navigation.testData.slotNavigation
import ru.vladislavsumin.core.navigation.testData.stackNavigation
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Характеризационные тесты механики initialPath — deep-link переходов, когда событие навигации
 * отправлено в [Navigation.navigationChannel] ДО монтирования корня через [childNavigationRoot].
 */
class InitialPathTest : NavigationIntegrationTestBase() {

    @Test
    fun pagesInitialPathOverridesDefault() = runTest {
        setMain()
        val navigation = pagesNavigation(initial = listOf(LeafParams(0)))
        navigation.open(LeafParams(1))
        val root = mount(navigation) as PagesRootScreen

        assertEquals(listOf(LeafParams(1)), root.pages.value.paramsList)
        assertEquals(0, root.pages.value.selectedIndex)
    }

    @Test
    fun stackInitialPathOverridesDefault() = runTest {
        setMain()
        val navigation = stackNavigation(initial = listOf(LeafParams(0)))
        navigation.open(LeafParams(1))
        val root = mount(navigation) as StackRootScreen

        assertEquals(listOf(LeafParams(1)), root.stack.value.paramsList)
    }

    @Test
    fun slotInitialPathOverridesDefault() = runTest {
        setMain()
        val navigation = slotNavigation(initial = LeafParams(0))
        navigation.open(LeafParams(1))
        val root = mount(navigation) as SlotRootScreen

        assertEquals(LeafParams(1), root.slot.value.child?.configuration?.screenParams)
    }

    @Test
    fun stackDefaultStackTruncatesWhenScreenExists() = runTest {
        setMain()
        val defaultStack = listOf(LeafParams(0), LeafParams(1), LeafParams(2))
        val navigation = stackWithDefaultStackNavigation(defaultStack = defaultStack)
        navigation.open(LeafParams(1))
        val root = mount(navigation) as StackRootScreen

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.stack.value.paramsList)
    }

    @Test
    fun stackDefaultStackAppendsNewScreen() = runTest {
        setMain()
        val defaultStack = listOf(LeafParams(0), LeafParams(1))
        val navigation = stackWithDefaultStackNavigation(defaultStack = defaultStack)
        navigation.open(LeafParams(3))
        val root = mount(navigation) as StackRootScreen

        assertEquals(listOf(LeafParams(0), LeafParams(1), LeafParams(3)), root.stack.value.paramsList)
    }

    @Test
    fun pagesDefaultPagesPlacesTargetAtCustomIndex() = runTest {
        setMain()
        val navigation = pagesNavigation(
            defaultPages = { params -> Pages(listOf(LeafParams(0), params, LeafParams(2)), 1) },
        )
        navigation.open(LeafParams(5))
        val root = mount(navigation) as PagesRootScreen

        assertEquals(listOf(LeafParams(0), LeafParams(5), LeafParams(2)), root.pages.value.paramsList)
        assertEquals(1, root.pages.value.selectedIndex)
    }

    @Test
    fun pagesDefaultPagesDeliversIntentToPlacedScreen() = runTest {
        setMain()
        val navigation = pagesNavigation(
            defaultPages = { params -> Pages(listOf(LeafParams(0), params, LeafParams(2)), 1) },
        )
        navigation.open(IntentLeafParams(7), intent = TestLeafIntent(99))
        val root = mount(navigation) as PagesRootScreen
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(LeafParams(0), IntentLeafParams(7), LeafParams(2)), root.pages.value.paramsList)
        assertEquals(1, root.pages.value.selectedIndex)
        val screen = root.pages.value.items
            .first { it.configuration.screenParams == IntentLeafParams(7) }
            .instance as IntentLeafScreen
        assertEquals(listOf(TestLeafIntent(99)), screen.receivedIntents)
    }

    @Test
    fun nestedInitialPathOpensDeepChain() = runTest {
        setMain()
        val navigation = chainNavigation()
        navigation.open(LeafParams(42))
        val root = mount(navigation) as NestedRootScreen

        val middle = root.pages.value.items[0].instance as MiddleScreen
        assertEquals(listOf(LeafParams(42)), middle.stack.value.paramsList)
    }

    @Test
    fun staleInitialPathDoesNotAffectSubsequentNavigation() = runTest {
        setMain()
        val navigation = chainNavigation()
        navigation.open(LeafParams(42))
        val root = mount(navigation) as NestedRootScreen

        root.open(MiddleParams(1))
        root.close(MiddleParams(0))

        val middle1 = root.pages.value.items
            .first { it.configuration.screenParams == MiddleParams(1) }
            .instance as MiddleScreen
        assertEquals(listOf(LeafParams(0)), middle1.stack.value.paramsList)
    }

    @Test
    fun initialPathDeliversIntent() = runTest {
        setMain()
        val navigation = pagesNavigation(initial = listOf(LeafParams(0)))
        navigation.open(IntentLeafParams(5), intent = TestLeafIntent(42))
        val root = mount(navigation) as PagesRootScreen
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(IntentLeafParams(5)), root.pages.value.paramsList)
        assertEquals(0, root.pages.value.selectedIndex)
        val screen = root.pages.value.items
            .first { it.configuration.screenParams == IntentLeafParams(5) }
            .instance as IntentLeafScreen
        assertEquals(listOf(TestLeafIntent(42)), screen.receivedIntents)
    }

    @Test
    fun chainInitialPathDeliversIntentToFinalScreen() = runTest {
        setMain()
        val navigation = chainNavigation()
        navigation.open(IntentLeafParams(7), intent = TestLeafIntent(99))
        val root = mount(navigation) as NestedRootScreen
        testScheduler.advanceUntilIdle()

        val middle = root.pages.value.items[0].instance as MiddleScreen
        val screen = middle.stack.value.items
            .first { it.configuration.screenParams == IntentLeafParams(7) }
            .instance as IntentLeafScreen
        assertEquals(listOf(TestLeafIntent(99)), screen.receivedIntents)
    }

    companion object {
        /**
         * Граф навигации аналогичен [nestedNavigation], но [MiddleParams] зарегистрирован с defaultParams,
         * чтобы в deep-link цепочке можно было разрешить `Key` → [MiddleParams].
         *
         * Граф: NestedRoot (Pages, HostA) → Middle(id) (Stack, HostB) → Leaf(id).
         */
        private fun chainNavigation(): Navigation = Navigation(
            setOf(
                GenericNavigationRegistrar {
                    registerScreen(
                        defaultParams = NestedRootParams,
                        factory = NestedRootFactory(),
                        navigationHosts = { NavigationHostA opens setOf(MiddleParams::class) },
                    )
                    registerScreen<MiddleParams>(
                        defaultParams = MiddleParams(0),
                        navigationHosts = {
                            NavigationHostB opens setOf(LeafParams::class, IntentLeafParams::class)
                        },
                    )
                    registerScreen<LeafParams>()
                    registerScreen<IntentLeafParams, TestLeafIntent, IntentLeafScreen>(
                        factory = object :
                            ScreenFactory<ComponentContext, IntentLeafParams, TestLeafIntent, IntentLeafScreen> {
                            override fun create(
                                context: ComponentContext,
                                params: IntentLeafParams,
                                intents: ReceiveChannel<TestLeafIntent>,
                            ): IntentLeafScreen = IntentLeafScreen(params, intents, context)
                        },
                    )
                },
            ),
        )

        /**
         * Граф со стековой навигацией, где [childNavigationStack] получает непустой [defaultStack].
         * Используется для проверки механики initialPath с defaultStack: усечение стека до целевого
         * экрана, если он уже есть в defaultStack, либо добавление нового экрана поверх defaultStack.
         *
         * Граф: StackRoot (Stack, HostA) → Leaf(id).
         */
        private fun stackWithDefaultStackNavigation(defaultStack: List<LeafParams>): Navigation = Navigation(
            setOf(
                GenericNavigationRegistrar {
                    registerScreen(
                        defaultParams = StackRootParams,
                        factory = StackRootFactory(
                            initial = listOf(LeafParams(0)),
                            defaultStack = defaultStack,
                        ),
                        navigationHosts = { NavigationHostA opens setOf(LeafParams::class) },
                    )
                    registerScreen<LeafParams>()
                },
            ),
        )
    }
}
