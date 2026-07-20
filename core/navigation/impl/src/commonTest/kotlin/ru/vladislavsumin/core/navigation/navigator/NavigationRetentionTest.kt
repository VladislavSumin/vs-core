package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.errorhandler.onDecomposeError
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.decompose.test.TestComponentContext
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.host.childNavigationRoot
import ru.vladislavsumin.core.navigation.testData.CountingViewModel
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.MiddleParams
import ru.vladislavsumin.core.navigation.testData.MiddleScreen
import ru.vladislavsumin.core.navigation.testData.NestedRootScreen
import ru.vladislavsumin.core.navigation.testData.PagesRootScreen
import ru.vladislavsumin.core.navigation.testData.leaf
import ru.vladislavsumin.core.navigation.testData.nestedNavigation
import ru.vladislavsumin.core.navigation.testData.pagesNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Характеризует удержание состояния табов при пересоздании корневого контекста навигации:
 * - config change: viewModel и её состояние сохраняются (тот же инстанс);
 * - process death: инстанс viewModel пересоздаётся, но сохраняемое состояние восстанавливается.
 *
 * Это ключевая гарантия «отсутствия регресса», которую рефакторинг под transfer должен сохранить.
 */
class NavigationRetentionTest {

    @Suppress("DEPRECATION")
    private val decomposeErrorHandler = run { onDecomposeError = {} }

    private var context = TestComponentContext()

    init {
        LoggerManager.initTest()
        CountingViewModel.nextId = 0
    }

    private fun mount(navigation: GenericNavigation<ComponentContext>): PagesRootScreen {
        val root = context.childNavigationRoot(navigation) as PagesRootScreen
        context.lifecycleRegistry.resume()
        return root
    }

    private fun recreateForConfigurationChange() {
        val savedState = context.stateKeeperDispatcher.save()
        val instanceKeeper = context.instanceKeeperDispatcher
        context.lifecycleRegistry.destroy()
        context = TestComponentContext(
            stateKeeperDispatcher = StateKeeperDispatcher(savedState),
            instanceKeeperDispatcher = instanceKeeper,
        )
    }

    private fun recreateForProcessDeath() {
        val savedState = context.stateKeeperDispatcher.save()
        context.instanceKeeperDispatcher.destroy()
        context.lifecycleRegistry.destroy()
        context = TestComponentContext(
            stateKeeperDispatcher = StateKeeperDispatcher(savedState),
            instanceKeeperDispatcher = InstanceKeeperDispatcher(),
        )
    }

    private fun mountNested(): NestedRootScreen {
        val root = context.childNavigationRoot(nestedNavigation()) as NestedRootScreen
        context.lifecycleRegistry.resume()
        return root
    }

    private fun NestedRootScreen.middle(index: Int): MiddleScreen = pages.value.items[index].instance as MiddleScreen

    private fun MiddleScreen.leaf(index: Int): LeafScreen = stack.value.items[index].instance as LeafScreen

    @Test
    fun configurationChangeRetainsTabViewModelInstanceAndState() = runTest {
        setMain()
        var root = mount(pagesNavigation())
        root.open(LeafParams(1))
        val vmBefore = root.pages.value.leaf(1).vm
        vmBefore.update(777)

        recreateForConfigurationChange()
        root = mount(pagesNavigation())

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
        val vmAfter = root.pages.value.leaf(1).vm
        assertSame(vmBefore, vmAfter)
        assertEquals(777, vmAfter.value)
    }

    @Test
    fun processDeathRestoresTabStateWithNewViewModelInstance() = runTest {
        setMain()
        var root = mount(pagesNavigation())
        root.open(LeafParams(1))
        val vmBefore = root.pages.value.leaf(1).vm
        vmBefore.update(555)

        recreateForProcessDeath()
        root = mount(pagesNavigation())

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
        val vmAfter = root.pages.value.leaf(1).vm
        assertNotSame(vmBefore, vmAfter)
        assertEquals(555, vmAfter.value)
    }

    @Test
    fun configurationChangeRecreatesHolderButRetainsViewModel() = runTest {
        setMain()
        var root = mount(pagesNavigation())
        root.open(LeafParams(1))
        val leafBefore = root.pages.value.leaf(1)
        val holderBefore = leafBefore.internalNavigator.holder
        val vmBefore = leafBefore.vm
        vmBefore.update(777)

        recreateForConfigurationChange()
        root = mount(pagesNavigation())

        val leafAfter = root.pages.value.leaf(1)
        val holderAfter = leafAfter.internalNavigator.holder

        assertNotSame(holderBefore, holderAfter)
        assertSame(vmBefore, leafAfter.vm)
        assertEquals(777, leafAfter.vm.value)
    }

    @Test
    fun processDeathDestroysOldViewModelViaInstanceKeeperHolder() = runTest {
        setMain()
        var root = mount(pagesNavigation())
        root.open(LeafParams(1))
        val vmBefore = root.pages.value.leaf(1).vm
        vmBefore.update(555)

        recreateForProcessDeath()

        assertFalse(
            vmBefore.isActive,
            "ViewModel should be destroyed after process death",
        )

        root = mount(pagesNavigation())
        val vmAfter = root.pages.value.leaf(1).vm
        assertNotSame(vmBefore, vmAfter)
        assertEquals(555, vmAfter.value)
    }

    @Test
    fun configurationChangeAfterTransferRetainsScreenInTargetLocation() = runTest {
        setMain()
        var root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leafVmBefore = root.middle(0).leaf(1).vm
        leafVmBefore.update(77)

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        recreateForConfigurationChange()
        root = mountNested()

        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        assertEquals(listOf(LeafParams(0), LeafParams(5)), root.middle(1).stack.value.paramsList)

        val leafVmAfter = root.middle(1).leaf(1).vm
        assertSame(leafVmBefore, leafVmAfter)
        assertEquals(77, leafVmAfter.value)
    }

    @Test
    fun configurationChangeRetainsNestedSubtreeState() = runTest {
        setMain()
        var root = mountNested()
        root.middle(0).open(LeafParams(1))
        root.open(MiddleParams(1))

        val middle0VmBefore = root.middle(0).vm
        val leaf0VmBefore = root.middle(0).leaf(0).vm
        val leaf1VmBefore = root.middle(0).leaf(1).vm
        middle0VmBefore.update(10)
        leaf0VmBefore.update(20)
        leaf1VmBefore.update(30)

        recreateForConfigurationChange()
        root = mountNested()

        assertEquals(listOf(MiddleParams(0), MiddleParams(1)), root.pages.value.paramsList)
        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.middle(0).stack.value.paramsList)
        assertEquals(listOf(LeafParams(0)), root.middle(1).stack.value.paramsList)

        assertSame(middle0VmBefore, root.middle(0).vm)
        assertSame(leaf0VmBefore, root.middle(0).leaf(0).vm)
        assertSame(leaf1VmBefore, root.middle(0).leaf(1).vm)
        assertEquals(10, root.middle(0).vm.value)
        assertEquals(20, root.middle(0).leaf(0).vm.value)
        assertEquals(30, root.middle(0).leaf(1).vm.value)
    }

    @Test
    fun processDeathAfterTransferRestoresStateInTargetLocation() = runTest {
        setMain()
        var root = mountNested()
        root.middle(0).open(LeafParams(5))
        root.open(MiddleParams(1))

        val leafVmBefore = root.middle(0).leaf(1).vm
        leafVmBefore.update(77)

        root.middle(0).transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        recreateForProcessDeath()
        root = mountNested()

        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        assertEquals(listOf(LeafParams(0), LeafParams(5)), root.middle(1).stack.value.paramsList)

        val leafVmAfter = root.middle(1).leaf(1).vm
        assertNotSame(leafVmBefore, leafVmAfter)
        assertEquals(77, leafVmAfter.value)
        assertFalse(leafVmBefore.isActive, "Old VM should be destroyed after process death")
    }
}
