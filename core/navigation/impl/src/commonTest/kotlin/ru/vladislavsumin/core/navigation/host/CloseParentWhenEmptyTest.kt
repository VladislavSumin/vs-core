package ru.vladislavsumin.core.navigation.host

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.InnerPagesParams
import ru.vladislavsumin.core.navigation.testData.InnerPagesScreen
import ru.vladislavsumin.core.navigation.testData.InnerStackParams
import ru.vladislavsumin.core.navigation.testData.InnerStackScreen
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.OuterSlotScreen
import ru.vladislavsumin.core.navigation.testData.closeParentNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Тесты поведения `closeParentWhenEmpty` для навигаций Stack и Pages.
 *
 * Граф: OuterSlot (Slot) -> Inner(Stack|Pages) -> Leaf. Внешний слот сам родителя не закрывает, поэтому закрытие
 * вложенным экраном самого себя приводит к очистке слота (`slot.value.child == null`).
 */
class CloseParentWhenEmptyTest : NavigationIntegrationTestBase() {

    private val OuterSlotScreen.innerStack get() = slot.value.child?.instance as InnerStackScreen?
    private val OuterSlotScreen.innerPages get() = slot.value.child?.instance as InnerPagesScreen?

    private fun InnerStackScreen.leaf(index: Int): LeafScreen = stack.value.items[index].instance as LeafScreen
    private fun InnerPagesScreen.leaf(index: Int): LeafScreen = pages.value.items[index].instance as LeafScreen

    // region stack

    @Test
    fun closingOnlyStackScreenClosesParent() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerStackParams(closeParentWhenEmpty = true)))
        val inner = outer.innerStack!!
        val innerVm = inner.vm
        val leaf0 = inner.leaf(0)

        leaf0.closeSelf()

        assertNull(outer.slot.value.child)
        assertFalse(innerVm.isActive)
        assertFalse(leaf0.vm.isActive)
    }

    @Test
    fun closingTopStackScreenKeepsParent() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerStackParams(closeParentWhenEmpty = true)))
        val inner = outer.innerStack!!
        inner.open(LeafParams(1))

        inner.close(LeafParams(1))

        assertNotNull(outer.slot.value.child)
        assertEquals(listOf(LeafParams(0)), inner.stack.value.paramsList)
        assertTrue(inner.vm.isActive)
    }

    @Test
    fun closingBottomStackScreenWithScreensAboveKeepsParent() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerStackParams(closeParentWhenEmpty = true)))
        val inner = outer.innerStack!!
        inner.open(LeafParams(1))

        // Стек не пуст (есть экран выше), поэтому родитель не закрывается — сохраняется штатное поведение стека.
        inner.close(LeafParams(0))

        assertNotNull(outer.slot.value.child)
        assertEquals(listOf(LeafParams(0)), inner.stack.value.paramsList)
    }

    @Test
    fun defaultStackFlagKeepsParentOnLastClose() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerStackParams(closeParentWhenEmpty = false)))
        val inner = outer.innerStack!!

        inner.close(LeafParams(0))

        // Флаг выключен: родитель остаётся, стек сохраняет единственный экран (штатное поведение).
        assertNotNull(outer.slot.value.child)
        assertEquals(listOf(LeafParams(0)), inner.stack.value.paramsList)
    }

    // endregion

    // region pages

    @Test
    fun closingLastPageClosesParent() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerPagesParams(closeParentWhenEmpty = true)))
        val inner = outer.innerPages!!
        val innerVm = inner.vm
        val leaf0 = inner.leaf(0)

        inner.close(LeafParams(0))

        assertNull(outer.slot.value.child)
        assertFalse(innerVm.isActive)
        assertFalse(leaf0.vm.isActive)
    }

    @Test
    fun closingPageWhenOthersRemainKeepsParent() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerPagesParams(closeParentWhenEmpty = true)))
        val inner = outer.innerPages!!
        inner.open(LeafParams(1))

        inner.close(LeafParams(1))

        assertNotNull(outer.slot.value.child)
        assertEquals(listOf(LeafParams(0)), inner.pages.value.paramsList)
        assertTrue(inner.vm.isActive)
    }

    @Test
    fun closingAllPagesClosesParentOnLast() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerPagesParams(closeParentWhenEmpty = true)))
        val inner = outer.innerPages!!
        inner.open(LeafParams(1))

        inner.close(LeafParams(1))
        assertNotNull(outer.slot.value.child)

        inner.close(LeafParams(0))
        assertNull(outer.slot.value.child)
    }

    @Test
    fun defaultPagesFlagKeepsParentAndEmptiesPages() = runTest {
        setMain()
        val outer: OuterSlotScreen = mount(closeParentNavigation(InnerPagesParams(closeParentWhenEmpty = false)))
        val inner = outer.innerPages!!

        inner.close(LeafParams(0))

        // Флаг выключен: родитель остаётся, а навигация pages опустошается (штатное поведение).
        assertNotNull(outer.slot.value.child)
        assertTrue(inner.pages.value.items.isEmpty())
    }

    // endregion
}
