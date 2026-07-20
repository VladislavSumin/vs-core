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
import ru.vladislavsumin.core.navigation.testData.PagesRootScreen
import ru.vladislavsumin.core.navigation.testData.leaf
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
}
