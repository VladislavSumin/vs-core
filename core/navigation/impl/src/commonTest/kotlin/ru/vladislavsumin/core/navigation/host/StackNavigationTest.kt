package ru.vladislavsumin.core.navigation.host

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.StackRootScreen
import ru.vladislavsumin.core.navigation.testData.paramsList
import ru.vladislavsumin.core.navigation.testData.stackNavigation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Характеризационные тесты навигации типа Stack (`childNavigationStack` + `StackHostNavigator`).
 */
class StackNavigationTest : NavigationIntegrationTestBase() {

    private fun mountStack(initial: List<LeafParams> = listOf(LeafParams(0))): StackRootScreen =
        mount(stackNavigation(initial))

    @Test
    fun initialStack() = runTest {
        setMain()
        val root = mountStack()
        assertEquals(listOf(LeafParams(0)), root.stack.value.paramsList)
    }

    @Test
    fun openNewScreenPushesToTop() = runTest {
        setMain()
        val root = mountStack()

        root.open(LeafParams(1))
        root.open(LeafParams(2))

        assertEquals(listOf(LeafParams(0), LeafParams(1), LeafParams(2)), root.stack.value.paramsList)
        assertEquals(LeafParams(2), root.stack.value.active.configuration.screenParams)
    }

    @Test
    fun openExistingScreenTruncatesAboveIt() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))
        root.open(LeafParams(2))

        root.open(LeafParams(1))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.stack.value.paramsList)
        assertEquals(LeafParams(1), root.stack.value.active.configuration.screenParams)
    }

    @Test
    fun lowerScreensRetainViewModelWhenPushing() = runTest {
        setMain()
        val root = mountStack()
        val bottomVm = (root.stack.value.items[0].instance as LeafScreen).vm

        root.open(LeafParams(1))

        assertSame(bottomVm, (root.stack.value.items[0].instance as LeafScreen).vm)
        assertTrue(bottomVm.isActive)
    }

    @Test
    fun closeMiddleScreenClosesItAndEverythingAbove() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))
        root.open(LeafParams(2))
        val vm1 = root.stack.value.items[1].instance as LeafScreen
        val vm2 = root.stack.value.items[2].instance as LeafScreen

        root.close(LeafParams(1))

        assertEquals(listOf(LeafParams(0)), root.stack.value.paramsList)
        assertFalse(vm1.vm.isActive)
        assertFalse(vm2.vm.isActive)
    }

    @Test
    fun closeTopScreenPopsOnlyIt() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))
        root.open(LeafParams(2))

        root.close(LeafParams(2))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.stack.value.paramsList)
        assertEquals(LeafParams(1), root.stack.value.active.configuration.screenParams)
    }

    @Test
    fun closeRootScreenIsNotAllowed() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))

        // Закрытие самого нижнего экрана стека невозможно: закрывается всё ВЫШЕ него, но не он сам.
        root.close(LeafParams(0))

        assertEquals(listOf(LeafParams(0)), root.stack.value.paramsList)
    }

    @Test
    fun closeNonExistentScreenIsNoOp() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))

        root.close(LeafParams(99))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.stack.value.paramsList)
    }

    @Test
    fun reopeningPoppedScreenCreatesFreshInstance() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))
        val firstVm = (root.stack.value.active.instance as LeafScreen).vm

        root.close(LeafParams(1))
        root.open(LeafParams(1))

        val secondVm = (root.stack.value.active.instance as LeafScreen).vm
        assertTrue(secondVm !== firstVm)
        assertFalse(firstVm.isActive)
        assertTrue(secondVm.isActive)
    }

    @Test
    fun closeSelfPopsOwnScreen() = runTest {
        setMain()
        val root = mountStack()
        root.open(LeafParams(1))
        root.open(LeafParams(2))
        val top = root.stack.value.active.instance as LeafScreen

        top.closeSelf()

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.stack.value.paramsList)
        assertFalse(top.vm.isActive)
    }
}
