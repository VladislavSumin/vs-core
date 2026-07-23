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
import ru.vladislavsumin.core.navigation.testData.nestedNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты переноса экранов, открытых через [openWithCustomFactory].
 *
 * Граф: NestedRoot (Pages) → Middle(id) (Stack) → Leaf(id).
 * Middle регистрирует custom factory для Leaf.
 * Leaf открывается через openWithCustomFactory (providerParams = MiddleParams(id=0)),
 * затем переносится в Middle(1). После пересоздания (process death) Leaf должен
 * восстановиться с той же фабрикой.
 */
class TransferWithCustomFactoryTest {

    @Suppress("DEPRECATION")
    private val decomposeErrorHandler = run { onDecomposeError = {} }

    private var context = TestComponentContext()

    init {
        LoggerManager.initTest()
        CountingViewModel.nextId = 0
    }

    private fun mount(navigation: GenericNavigation<ComponentContext>): NestedRootScreen {
        val root = context.childNavigationRoot(navigation) as NestedRootScreen
        context.lifecycleRegistry.resume()
        return root
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

    private fun NestedRootScreen.middle(index: Int): MiddleScreen =
        pages.value.items[index].instance as MiddleScreen

    private fun MiddleScreen.leaf(index: Int): LeafScreen =
        stack.value.items[index].instance as LeafScreen

    @Test
    fun transferTargetWithCustomFactory() = runTest {
        setMain()
        val root = mount(nestedNavigation())
        val middle0 = root.middle(0)

        middle0.openWithCustomFactory(LeafParams(5))
        assertEquals(listOf(LeafParams(0), LeafParams(5)), middle0.stack.value.paramsList)

        root.open(MiddleParams(1))

        middle0.transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        assertEquals(listOf(LeafParams(0)), middle0.stack.value.paramsList)
        val middle1 = root.middle(1)
        assertEquals(listOf(LeafParams(0), LeafParams(5)), middle1.stack.value.paramsList)
    }

    @Test
    fun transferTargetPreservesVMState() = runTest {
        setMain()
        val root = mount(nestedNavigation())
        val middle0 = root.middle(0)

        middle0.openWithCustomFactory(LeafParams(5))
        val leafVm = middle0.leaf(1).vm
        leafVm.update(42)

        root.open(MiddleParams(1))
        middle0.transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        val middle1 = root.middle(1)
        assertTrue(leafVm.isActive, "VM must survive transfer")
        assertEquals(42, leafVm.value, "VM state must survive transfer")
    }

    @Test
    fun processDeathAfterTransferRestoresTarget() = runTest {
        setMain()
        val root = mount(nestedNavigation())
        val middle0 = root.middle(0)

        middle0.openWithCustomFactory(LeafParams(5))
        val vmBefore = middle0.leaf(1).vm
        vmBefore.update(42)

        root.open(MiddleParams(1))
        middle0.transfer(LeafParams(5), hints = listOf(MiddleParams(1)))

        recreateForProcessDeath()

        val root2 = mount(nestedNavigation())
        val middle1 = root2.middle(1)
        assertEquals(
            listOf(LeafParams(0), LeafParams(5)),
            middle1.stack.value.paramsList,
            "Target must be restored in Middle(1) after process death",
        )
        val leafAfter = middle1.leaf(1)
        assertEquals(42, leafAfter.vm.value, "VM state must be restored after process death")
    }
}
