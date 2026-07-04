package ru.vladislavsumin.core.navigation.host

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.SlotRootScreen
import ru.vladislavsumin.core.navigation.testData.slotNavigation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Характеризационные тесты навигации типа Slot (`childNavigationSlot` + `SlotHostNavigator`).
 */
class SlotNavigationTest : NavigationIntegrationTestBase() {

    private fun mountSlot(initial: LeafParams? = LeafParams(0)): SlotRootScreen = mount(slotNavigation(initial))

    private val SlotRootScreen.activeParams get() = slot.value.child?.configuration?.screenParams
    private val SlotRootScreen.activeLeaf get() = slot.value.child?.instance as LeafScreen?

    @Test
    fun initialConfigurationOpensScreen() = runTest {
        setMain()
        val root = mountSlot(LeafParams(0))
        assertEquals(LeafParams(0), root.activeParams)
    }

    @Test
    fun initialNullConfigurationLeavesSlotEmpty() = runTest {
        setMain()
        val root = mountSlot(initial = null)
        assertNull(root.slot.value.child)
    }

    @Test
    fun openReplacesCurrentScreenAndDestroysOld() = runTest {
        setMain()
        val root = mountSlot(LeafParams(0))
        val oldVm = root.activeLeaf!!.vm

        root.open(LeafParams(1))

        assertEquals(LeafParams(1), root.activeParams)
        assertFalse(oldVm.isActive)
        assertTrue(root.activeLeaf!!.vm.isActive)
    }

    @Test
    fun openSameScreenKeepsInstance() = runTest {
        setMain()
        val root = mountSlot(LeafParams(0))
        val leaf = root.activeLeaf!!
        val vm = leaf.vm

        root.open(LeafParams(0))

        assertSame(leaf, root.activeLeaf)
        assertSame(vm, root.activeLeaf!!.vm)
        assertTrue(vm.isActive)
    }

    @Test
    fun closeActiveScreenEmptiesSlot() = runTest {
        setMain()
        val root = mountSlot(LeafParams(0))
        val vm = root.activeLeaf!!.vm

        root.close(LeafParams(0))

        assertNull(root.slot.value.child)
        assertFalse(vm.isActive)
    }

    @Test
    fun closeNonActiveScreenIsNoOp() = runTest {
        setMain()
        val root = mountSlot(LeafParams(0))

        root.close(LeafParams(1))

        assertEquals(LeafParams(0), root.activeParams)
    }
}
