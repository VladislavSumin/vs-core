package ru.vladislavsumin.core.navigation.viewModel

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.NavVmRootScreen
import ru.vladislavsumin.core.navigation.testData.NavVmScreen
import ru.vladislavsumin.core.navigation.testData.NavVmTargetParams
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.lifecycleState
import ru.vladislavsumin.core.navigation.testData.navViewModelNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NavigationViewModelTest : NavigationIntegrationTestBase() {

    @Test
    fun vmOpenDispatchesToNavigator() = runTest {
        setMain()
        val root: NavVmRootScreen = mount(navViewModelNavigation())
        val page0 = root.pages.value.items[0].instance as NavVmScreen
        val vm = page0.vm

        vm.doOpen(NavVmTargetParams(1))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(NavVmTargetParams(0), NavVmTargetParams(1)), root.pages.value.paramsList)
    }

    @Test
    fun vmCloseDispatchesToNavigator() = runTest {
        setMain()
        val root: NavVmRootScreen = mount(navViewModelNavigation())
        root.open(NavVmTargetParams(1))
        val page0 = root.pages.value.items[0].instance as NavVmScreen
        val page1 = root.pages.value.items[1].instance as NavVmScreen
        val vm = page1.vm

        vm.doClose(NavVmTargetParams(0))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(NavVmTargetParams(1)), root.pages.value.paramsList)
        assertEquals(Lifecycle.State.DESTROYED, page0.lifecycleState)
    }

    @Test
    fun vmCloseSelfDispatchesToNavigator() = runTest {
        setMain()
        val root: NavVmRootScreen = mount(navViewModelNavigation())
        root.open(NavVmTargetParams(1))
        val page0 = root.pages.value.items[0].instance as NavVmScreen
        val vm = page0.vm

        vm.doCloseSelf()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(NavVmTargetParams(1)), root.pages.value.paramsList)
        assertFalse(vm.isActive)
        assertEquals(Lifecycle.State.DESTROYED, page0.lifecycleState)
    }

    @Test
    fun vmCloseNonExistentIsNoOp() = runTest {
        setMain()
        val root: NavVmRootScreen = mount(navViewModelNavigation())
        root.open(NavVmTargetParams(1))
        val page1 = root.pages.value.items[1].instance as NavVmScreen
        val vm = page1.vm

        vm.doClose(NavVmTargetParams(99))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(NavVmTargetParams(0), NavVmTargetParams(1)), root.pages.value.paramsList)
    }
}
