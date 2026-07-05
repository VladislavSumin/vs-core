package ru.vladislavsumin.core.navigation.navigator

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.ChainRootScreen
import ru.vladislavsumin.core.navigation.testData.IntentLeafParams
import ru.vladislavsumin.core.navigation.testData.IntentLeafScreen
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.PagesMiddleParams
import ru.vladislavsumin.core.navigation.testData.PagesMiddleScreen
import ru.vladislavsumin.core.navigation.testData.StackMiddleParams
import ru.vladislavsumin.core.navigation.testData.StackMiddleScreen
import ru.vladislavsumin.core.navigation.testData.TestLeafIntent
import ru.vladislavsumin.core.navigation.testData.chainDefaultNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты поведения runtime-цепочек (open уже после монтирования корня): промежуточные экраны, создаваемые по пути
 * к цели, должны использовать default-лямбду (как при deep-link на старте), а не initial-лямбду. Последний экран
 * цепочки при этом использует свою initial-лямбду.
 *
 * Граф: ChainRoot (Pages) -> StackMiddle(id) (Stack) / PagesMiddle(id) (Pages) -> Leaf(id).
 */
class RuntimeChainDefaultTest : NavigationIntegrationTestBase() {

    private fun ChainRootScreen.stackMiddle(id: Int): StackMiddleScreen = pages.value.items
        .first { it.configuration.screenParams == StackMiddleParams(id) }
        .instance as StackMiddleScreen

    private fun ChainRootScreen.pagesMiddle(id: Int): PagesMiddleScreen = pages.value.items
        .first { it.configuration.screenParams == PagesMiddleParams(id) }
        .instance as PagesMiddleScreen

    @Test
    fun newIntermediateStackScreenUsesDefaultStack() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        // Открываем Leaf(9) в новой (ещё не существующей) вкладке StackMiddle(1) — она промежуточная в цепочке.
        root.open(LeafParams(9), hints = listOf(StackMiddleParams(1)))

        // default-лямбда: [Leaf(200)] + цель Leaf(9); а не initial-лямбда [Leaf(100)].
        assertEquals(listOf(LeafParams(200), LeafParams(9)), root.stackMiddle(1).stack.value.paramsList)
    }

    @Test
    fun directlyOpenedStackScreenUsesInitialStack() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        // Открываем сам StackMiddle(1) напрямую — он последний в цепочке, значит использует initial-лямбду.
        root.open(StackMiddleParams(1))

        assertEquals(listOf(LeafParams(100)), root.stackMiddle(1).stack.value.paramsList)
    }

    @Test
    fun newIntermediatePagesScreenUsesDefaultPages() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        root.open(LeafParams(9), hints = listOf(PagesMiddleParams(1)))

        // default-лямбда разместила цель среди своих страниц: [Leaf(200), Leaf(9)] с выбранным индексом 1;
        // а не initial-лямбда [Leaf(100)].
        assertEquals(listOf(LeafParams(200), LeafParams(9)), root.pagesMiddle(1).pages.value.paramsList)
        assertEquals(1, root.pagesMiddle(1).pages.value.selectedIndex)
    }

    @Test
    fun directlyOpenedPagesScreenUsesInitialPages() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        root.open(PagesMiddleParams(1))

        assertEquals(listOf(LeafParams(100)), root.pagesMiddle(1).pages.value.paramsList)
    }

    @Test
    fun intentDeliveredToTargetThroughNewIntermediate() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        root.openWithIntent(IntentLeafParams(7), TestLeafIntent(42), hints = listOf(StackMiddleParams(1)))
        testScheduler.advanceUntilIdle()

        // Промежуточный StackMiddle(1) собран через default-лямбду, цель добавлена поверх.
        assertEquals(listOf(LeafParams(200), IntentLeafParams(7)), root.stackMiddle(1).stack.value.paramsList)
        val screen = root.stackMiddle(1).stack.value.items
            .first { it.configuration.screenParams == IntentLeafParams(7) }
            .instance as IntentLeafScreen
        assertEquals(listOf(TestLeafIntent(42)), screen.receivedIntents)
    }

    @Test
    fun existingIntermediateStackIsNotReset() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        // StackMiddle(0) существует с момента mount и собран по initial-лямбде.
        assertEquals(listOf(LeafParams(100)), root.stackMiddle(0).stack.value.paramsList)

        // Открываем Leaf(9) в уже существующем StackMiddle(0): цель добавляется поверх текущего стека, без сброса.
        root.open(LeafParams(9), hints = listOf(StackMiddleParams(0)))

        assertEquals(listOf(LeafParams(100), LeafParams(9)), root.stackMiddle(0).stack.value.paramsList)
    }

    @Test
    fun transferIntoNewIntermediateUsesDefaultAndAdoptsInstance() = runTest {
        setMain()
        val root = mount(chainDefaultNavigation()) as ChainRootScreen

        // В существующем StackMiddle(0) открываем Leaf(5) и запоминаем его VM.
        root.open(LeafParams(5), hints = listOf(StackMiddleParams(0)))
        val leafVm = root.stackMiddle(0).stack.value.items
            .first { it.configuration.screenParams == LeafParams(5) }
            .let { it.instance as LeafScreen }
            .vm
        leafVm.update(77)

        // Переносим Leaf(5) в новую (ещё не существующую) вкладку StackMiddle(1) — она промежуточная.
        root.transfer(LeafParams(5), hints = listOf(StackMiddleParams(1)))

        // StackMiddle(1) собран через default-лямбду ([Leaf(200)]), перенесённый Leaf(5) усыновлён поверх.
        assertEquals(listOf(LeafParams(200), LeafParams(5)), root.stackMiddle(1).stack.value.paramsList)
        // Инстанс (viewModel) пережил перенос через default-механизм.
        val transferredVm = root.stackMiddle(1).stack.value.items
            .first { it.configuration.screenParams == LeafParams(5) }
            .let { it.instance as LeafScreen }
            .vm
        assertTrue(transferredVm === leafVm)
        assertEquals(77, transferredVm.value)
    }
}
