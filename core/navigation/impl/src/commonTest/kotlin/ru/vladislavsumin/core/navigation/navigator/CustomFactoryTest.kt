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
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.ProviderRootScreen
import ru.vladislavsumin.core.navigation.testData.ProviderScreen
import ru.vladislavsumin.core.navigation.testData.TargetParams
import ru.vladislavsumin.core.navigation.testData.TargetScreen
import ru.vladislavsumin.core.navigation.testData.providerCustomFactoryNavigation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CustomFactoryTest : NavigationIntegrationTestBase() {

    private fun mountProvider(): ProviderRootScreen = mount(providerCustomFactoryNavigation())

    @Test
    fun providerOpensTargetWithCustomFactory() = runTest {
        setMain()
        val root = mountProvider()
        val provider = root.provider()

        provider.openTargetWithCustomFactory(TargetParams(1))

        val target = provider.targetSlotChild()
        assertNotNull(target, "Target should be opened in provider's slot")
        assertIs<TargetScreen>(target)
        assertEquals(TargetParams(1), target.params)
        assertTrue(provider.createdTarget != null, "Target should be created via provider's custom factory")
    }

    @Test
    fun targetVMAndStateSurvivesConfigChange() = runTest {
        setMain()
        val helper = RetentionHelper()
        helper.mountAndOpen()
        helper.recreateForConfigChange()
        val (target, _) = helper.mountAndGet()

        val oldTarget = helper.lastTarget!!
        assertSame(oldTarget.vm, target.vm, "VM must survive config change")
        assertEquals(42, target.vm.value, "VM state must survive config change")
    }

    @Test
    fun targetStateRestoresAfterProcessDeath() = runTest {
        setMain()
        val helper = RetentionHelper()
        helper.mountAndOpen()
        helper.recreateForProcessDeath()
        val (target, _) = helper.mountAndGet()

        assertEquals(42, target.vm.value, "VM state must be restored after process death")
    }

    @Test
    fun closingProviderDestroysTarget() = runTest {
        setMain()
        val root = mountProvider()
        val provider = root.provider()
        provider.openTargetWithCustomFactory(TargetParams(1))
        val target = provider.targetSlotChild() as TargetScreen
        val targetVm = target.vm
        assertTrue(targetVm.isActive)

        provider.closeTarget(TargetParams(1))

        kotlin.test.assertFalse(targetVm.isActive, "Target VM should be destroyed when closed")
    }

    @Test
    fun targetReopenedAfterCloseUsesNewVM() = runTest {
        setMain()
        val root = mountProvider()
        val provider = root.provider()

        provider.openTargetWithCustomFactory(TargetParams(1))
        val target1 = provider.targetSlotChild() as TargetScreen
        val vm1 = target1.vm

        provider.closeTarget(TargetParams(1))

        provider.openTargetWithCustomFactory(TargetParams(2))
        val target2 = provider.targetSlotChild() as TargetScreen
        assertEquals(TargetParams(2), target2.params)
        assertTrue(target2.vm !== vm1, "New target must have new VM")
    }

    @Test
    fun multipleTargetsInSequence() = runTest {
        setMain()
        val root = mountProvider()
        val provider = root.provider()

        provider.openTargetWithCustomFactory(TargetParams(1))
        val target1 = provider.targetSlotChild() as TargetScreen

        provider.openTargetWithCustomFactory(TargetParams(2))
        val target2 = provider.targetSlotChild() as TargetScreen

        assertEquals(TargetParams(2), target2.params)
        kotlin.test.assertFalse(target1.vm.isActive, "Old target VM should be destroyed (slot replaces)")
    }

    private class RetentionHelper {
        @Suppress("DEPRECATION")
        private val decomposeErrorHandler = run { onDecomposeError = {} }

        private var context = TestComponentContext()
        var lastTarget: TargetScreen? = null
            private set

        init {
            LoggerManager.initTest()
            CountingViewModel.nextId = 0
        }

        private fun mount(navigation: GenericNavigation<ComponentContext>): ProviderRootScreen {
            val root = context.childNavigationRoot(navigation) as ProviderRootScreen
            context.lifecycleRegistry.resume()
            return root
        }

        fun mountAndOpen() {
            val root = mount(providerCustomFactoryNavigation())
            val provider = root.provider()
            provider.openTargetWithCustomFactory(TargetParams(1))
            val target = provider.targetSlotChild() as TargetScreen
            target.vm.update(42)
            lastTarget = target
        }

        fun mountAndGet(): Pair<TargetScreen, ProviderScreen> {
            val root = mount(providerCustomFactoryNavigation())
            val provider = root.provider()
            val target = provider.targetSlotChild() as TargetScreen
            return target to provider
        }

        fun recreateForConfigChange() {
            val savedState = context.stateKeeperDispatcher.save()
            val instanceKeeper = context.instanceKeeperDispatcher
            context.lifecycleRegistry.destroy()
            context = TestComponentContext(
                stateKeeperDispatcher = StateKeeperDispatcher(savedState),
                instanceKeeperDispatcher = instanceKeeper,
            )
        }

        fun recreateForProcessDeath() {
            val savedState = context.stateKeeperDispatcher.save()
            context.instanceKeeperDispatcher.destroy()
            context.lifecycleRegistry.destroy()
            context = TestComponentContext(
                stateKeeperDispatcher = StateKeeperDispatcher(savedState),
                instanceKeeperDispatcher = InstanceKeeperDispatcher(),
            )
        }
    }
}
