package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.MiddleParams
import ru.vladislavsumin.core.navigation.testData.MiddleScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.NestedRootParams
import ru.vladislavsumin.core.navigation.testData.NestedRootScreen
import ru.vladislavsumin.core.navigation.testData.lifecycleState
import ru.vladislavsumin.core.navigation.testData.nestedNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Характеризационные тесты вложенной навигации: [ru.vladislavsumin.core.navigation.host.childScreenFactory],
 * [ScreenNavigatorImpl] (screenPath, регистрация/дерегистрация дочерних навигаторов) и открытие через hints
 * между разными инстансами родительского экрана.
 *
 * Граф: NestedRoot (Pages) -> Middle(id) (Stack) -> Leaf(id).
 */
class ScreenNavigatorNestedTest : NavigationIntegrationTestBase() {

    private fun mountNested(): NestedRootScreen = mount(nestedNavigation())

    private fun NestedRootScreen.middle(index: Int): MiddleScreen = pages.value.items[index].instance as MiddleScreen

    @Test
    fun nestedInitialStructure() = runTest {
        setMain()
        val root = mountNested()

        assertEquals(listOf(MiddleParams(0)), root.pages.value.paramsList)
        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
    }

    @Test
    fun openScreenInNestedHost() = runTest {
        setMain()
        val root = mountNested()

        root.middle(0).open(LeafParams(1))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.middle(0).stack.value.paramsList)
    }

    @Test
    fun screenPathReflectsNesting() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(1))

        val leaf = root.middle(0).stack.value.active.instance as LeafScreen
        val expected = ScreenPath(NestedRootParams) + MiddleParams(0) + LeafParams(1)
        assertEquals(expected, leaf.internalNavigator.screenPath)
    }

    @Test
    fun middleScreenPathReflectsNesting() = runTest {
        setMain()
        val root = mountNested()

        val expected = ScreenPath(NestedRootParams) + MiddleParams(0)
        assertEquals(expected, root.middle(0).internalNavigator.screenPath)
    }

    @Test
    fun openWithHintCreatesSecondParentInstanceAndOpensChildInside() = runTest {
        setMain()
        val root = mountNested()

        // Открываем Leaf внутри новой (ещё не существующей) вкладки Middle(1) — как "открыть в новом окне".
        root.open(LeafParams(9), hints = listOf(MiddleParams(1)))

        assertEquals(listOf(MiddleParams(0), MiddleParams(1)), root.pages.value.paramsList)
        // Middle(0) не тронут.
        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        // Middle(1) — промежуточный экран цепочки, поэтому использует default-лямбду (у Middle он пустой),
        // а не initial-лямбду ([Leaf(0)]). Итог симметричен deep-link на старте (nestedInitialPathOpensDeepChain).
        assertEquals(listOf(LeafParams(9)), root.middle(1).stack.value.paramsList)
    }

    @Test
    fun closingParentDestroysWholeSubtree() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(1))
        root.open(MiddleParams(1)) // вторая вкладка, чтобы не закрывать единственную

        val middle0 = root.middle(0)
        val middle0Vm = middle0.vm
        val leaf0Vm = (middle0.stack.value.items[0].instance as LeafScreen).vm
        val leaf1Vm = (middle0.stack.value.items[1].instance as LeafScreen).vm

        root.close(MiddleParams(0))

        assertEquals(listOf(MiddleParams(1)), root.pages.value.paramsList)
        assertFalse(middle0Vm.isActive)
        assertFalse(leaf0Vm.isActive)
        assertFalse(leaf1Vm.isActive)
    }

    @Test
    fun reopeningClosedParentCreatesFreshInstance() = runTest {
        setMain()
        val root = mountNested()
        root.open(MiddleParams(1))
        val middle0First = root.middle(0)

        root.close(MiddleParams(0))
        root.open(MiddleParams(0))

        val middle0Second = root.pages.value.items
            .first { it.configuration.screenParams == MiddleParams(0) }
            .instance as MiddleScreen
        assertTrue(middle0Second !== middle0First)
        assertFalse(middle0First.vm.isActive)
        assertTrue(middle0Second.vm.isActive)
    }

    @Test
    fun inactiveParentRetainsSubtreeState() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(1))
        val middle0Vm = root.middle(0).vm

        root.open(MiddleParams(1)) // Middle(0) становится неактивной (CREATED)

        // Состояние поддерева Middle(0) сохранено.
        assertTrue(middle0Vm.isActive)
        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.middle(0).stack.value.paramsList)
    }

    @Test
    fun closingParentDestroysChildLifecycle() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(1))
        root.open(MiddleParams(1))

        val leaf0 = root.middle(0).stack.value.items[0].instance as LeafScreen
        val leaf1 = root.middle(0).stack.value.items[1].instance as LeafScreen

        root.close(MiddleParams(0))

        assertEquals(Lifecycle.State.DESTROYED, leaf0.lifecycleState)
        assertEquals(Lifecycle.State.DESTROYED, leaf1.lifecycleState)
    }
}
