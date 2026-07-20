package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.MiddleParams
import ru.vladislavsumin.core.navigation.testData.MiddleScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.NestedRootScreen
import ru.vladislavsumin.core.navigation.testData.lifecycleState
import ru.vladislavsumin.core.navigation.testData.nestedNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Тесты переноса экранов (transfer) между разными инстансами навигации.
 *
 * Граф: NestedRoot (Pages) → Middle(id) (Stack) → Leaf(id)
 * Leaf может быть перенесён между Middle(0) и Middle(1).
 */
class TransferTest : NavigationIntegrationTestBase() {

    private fun mountNested(): NestedRootScreen = mount(nestedNavigation())

    private fun NestedRootScreen.middle(index: Int): MiddleScreen = pages.value.items[index].instance as MiddleScreen

    private fun MiddleScreen.leaf(index: Int): LeafScreen = stack.value.items[index].instance as LeafScreen

    @Test
    fun `transfer moves screen between parent instances`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1)) // создаём вторую Middle-вкладку

        // Leaf(5) в Middle(0)
        assertEquals(listOf(LeafParams(0), LeafParams(5)), root.middle(0).stack.value.paramsList)
        // Middle(1) имеет только стартовый Leaf(0)
        assertEquals(listOf(LeafParams(0)), root.middle(1).stack.value.paramsList)

        // Переносим Leaf(5) из Middle(0) в Middle(1)
        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        // Leaf(5) ушёл из Middle(0)
        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        // Leaf(5) появился в Middle(1)
        assertEquals(listOf(LeafParams(0), LeafParams(5)), root.middle(1).stack.value.paramsList)
    }

    @Test
    fun `transfer preserves viewModel state`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leafVm = root.middle(0).leaf(1).vm
        leafVm.update(42)

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        // VM жива и сохранила значение
        assertTrue(leafVm.isActive)
        assertEquals(42, leafVm.value)
    }

    @Test
    fun `transfer preserves navigator screenPath`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        val beforePath = root.middle(0).leaf(1).internalNavigator.screenPath

        root.open(MiddleParams(1))
        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        val transferredLeaf = root.middle(1).leaf(1)
        val afterPath = transferredLeaf.internalNavigator.screenPath

        // Путь изменился: последний элемент LeafParams(5) тот же,
        // но префикс теперь указывает на MiddleParams(1), а не MiddleParams(0)
        assertEquals(beforePath.last(), afterPath.last())
        assertFalse(beforePath.toString() == afterPath.toString())
    }

    @Test
    fun `transfer to same location is no-op`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))

        val beforePages = root.pages.value.paramsList
        val beforeStack = root.middle(0).stack.value.paramsList

        // Попытка перенести Leaf(5) в ту же Middle(0) — no-op
        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(0)))

        // Ничего не изменилось
        assertEquals(beforePages, root.pages.value.paramsList)
        assertEquals(beforeStack, root.middle(0).stack.value.paramsList)
    }

    @Test
    fun `VM from pre-transfer screen is same object after transfer`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leafBefore = root.middle(0).leaf(1)
        val vmBefore = leafBefore.vm
        vmBefore.update(77)

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        val leafAfter = root.middle(1).leaf(1)
        val vmAfter = leafAfter.vm

        // Экран пересоздан
        assertTrue(leafAfter !== leafBefore)
        // ViewModel та же (instanceKeeper общий)
        assertTrue(vmAfter === vmBefore)
        assertEquals(77, vmAfter.value)
    }

    @Test
    fun `transfer across pages via parent navigator`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        val leafVm = root.middle(0).leaf(1).vm
        leafVm.update(77)
        root.open(MiddleParams(1))

        // Перенос через навигатор NestedRoot (а не Middle)
        root.transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        assertEquals(listOf(LeafParams(0), LeafParams(5)), root.middle(1).stack.value.paramsList)
        assertEquals(77, leafVm.value)
    }

    @Test
    fun `saveableRegistry captureRaw survives transfer`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leaf = root.middle(0).leaf(1)
        val holder = leaf.internalNavigator.holder
        assertNotNull(holder)

        val registry = holder.saveableStateRegistry
        registry.registerProvider("test_key") { "test_value" }

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        val transferredLeaf = root.middle(1).leaf(1)
        val transferredHolder = transferredLeaf.internalNavigator.holder
        assertNotNull(transferredHolder)

        // Проверяем, что значение из captureRaw сохранилось при переносе
        assertEquals("test_value", transferredHolder.saveableStateRegistry.consumeRestored("test_key"))
    }

    @Test
    fun `instanceKeeper survives transfer`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leafBefore = root.middle(0).leaf(1)
        val holderBefore = leafBefore.internalNavigator.holder
        assertNotNull(holderBefore)

        val instanceKeeperBefore = holderBefore.instanceKeeper

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        val leafAfter = root.middle(1).leaf(1)
        val holderAfter = leafAfter.internalNavigator.holder
        assertNotNull(holderAfter)

        // instanceKeeper пережил перенос (тот же объект)
        assertTrue(instanceKeeperBefore === holderAfter.instanceKeeper)
    }

    @Test
    fun `transfer to nonexistent parent instance creates it and transfers screen`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))

        // Перенос в несуществующий Middle(999) — он создаётся,
        // Leaf(5) переносится внутрь нового Middle(999)
        root.transfer(LeafParams(5), hints = listOf(MiddleParams(999)))

        // Middle(0) больше не содержит Leaf(5)
        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        // Появился новый Middle(999)
        assertTrue(root.pages.value.items.any { it.configuration.screenParams == MiddleParams(999) })
        // Middle(999) — новый промежуточный родитель, поэтому использует default-лямбду (у Middle он пустой),
        // а перенесённый Leaf(5) усыновлён поверх. Итог: [Leaf(5)] (а не initial [Leaf(0)] + Leaf(5)).
        val middle999 = root.pages.value.items
            .first { it.configuration.screenParams == MiddleParams(999) }
            .instance as MiddleScreen
        assertEquals(listOf(LeafParams(5)), middle999.stack.value.paramsList)
    }

    @Test
    fun `transfer nonexistent screen is no-op`() = runTest {
        setMain()
        val root = mountNested()
        val before = root.pages.value.paramsList

        // Leaf(99) не открыт — transfer ничего не делает
        root.transfer(LeafParams(99), hints = listOf(MiddleParams(1)))

        assertEquals(before, root.pages.value.paramsList)
    }

    @Test
    fun `transfer destroys old screen lifecycle`() = runTest {
        setMain()
        val root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leafBefore = root.middle(0).leaf(1)

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        assertEquals(Lifecycle.State.DESTROYED, leafBefore.lifecycleState)
    }
}
