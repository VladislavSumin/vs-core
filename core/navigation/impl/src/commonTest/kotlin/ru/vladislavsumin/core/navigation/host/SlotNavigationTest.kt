package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.testData.IntentLeafParams
import ru.vladislavsumin.core.navigation.testData.IntentLeafScreen
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.SlotRootFactory
import ru.vladislavsumin.core.navigation.testData.SlotRootParams
import ru.vladislavsumin.core.navigation.testData.SlotRootScreen
import ru.vladislavsumin.core.navigation.testData.TestLeafIntent
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

    @Test
    fun intentDeliveredToNewlyOpenedScreen() = runTest {
        setMain()
        val navigation = slotWithIntentNav()
        val root = mount(navigation) as SlotRootScreen
        root.openWithIntent(IntentLeafParams(5), TestLeafIntent(42))
        testScheduler.advanceUntilIdle()

        val screen = root.slot.value.child?.instance as IntentLeafScreen
        assertEquals(listOf(TestLeafIntent(42)), screen.receivedIntents)
    }

    companion object {
        private fun slotWithIntentNav(): Navigation = Navigation(
            setOf(
                GenericNavigationRegistrar {
                    registerScreen(
                        defaultParams = SlotRootParams,
                        factory = SlotRootFactory(initial = LeafParams(0)),
                        navigationHosts = {
                            NavigationHostA opens setOf(LeafParams::class, IntentLeafParams::class)
                        },
                    )
                    registerScreen<LeafParams>()
                    registerScreen<IntentLeafParams, TestLeafIntent, IntentLeafScreen>(
                        factory = object :
                            ScreenFactory<ComponentContext, IntentLeafParams, TestLeafIntent, IntentLeafScreen> {
                            override fun create(
                                context: ComponentContext,
                                params: IntentLeafParams,
                                intents: ReceiveChannel<TestLeafIntent>,
                            ): IntentLeafScreen = IntentLeafScreen(params, intents, context)
                        },
                    )
                },
            ),
        )
    }
}
