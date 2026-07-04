package ru.vladislavsumin.core.navigation.host

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.IntentLeafParams
import ru.vladislavsumin.core.navigation.testData.IntentLeafScreen
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.PagesRootScreen
import ru.vladislavsumin.core.navigation.testData.TestLeafIntent
import ru.vladislavsumin.core.navigation.testData.leaf
import ru.vladislavsumin.core.navigation.testData.lifecycleState
import ru.vladislavsumin.core.navigation.testData.pagesNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Характеризационные тесты навигации типа Pages (`childNavigationPages` + `PagesHostNavigator`).
 *
 * Закрепляют текущее поведение перед рефакторингом под transfer.
 */
class PagesNavigationTest : NavigationIntegrationTestBase() {

    private fun mountPages(
        keepInactive: Boolean = true,
        initial: List<LeafParams> = listOf(LeafParams(0)),
        selectedIndex: Int = 0,
    ): PagesRootScreen = mount(pagesNavigation(keepInactive, initial, selectedIndex))

    @Test
    fun initialPageIsCreatedAndSelected() = runTest {
        setMain()
        val root = mountPages()
        assertEquals(listOf(LeafParams(0)), root.pages.value.paramsList)
        assertEquals(0, root.pages.value.selectedIndex)
    }

    @Test
    fun openNewScreenAppendsAndSelectsIt() = runTest {
        setMain()
        val root = mountPages()

        root.open(LeafParams(1))
        root.open(LeafParams(2))

        assertEquals(listOf(LeafParams(0), LeafParams(1), LeafParams(2)), root.pages.value.paramsList)
        assertEquals(2, root.pages.value.selectedIndex)
    }

    @Test
    fun openExistingScreenSelectsItWithoutRecreating() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))

        val leaf0Before = root.pages.value.leaf(0)
        val vm0Before = leaf0Before.vm

        root.open(LeafParams(0))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
        assertEquals(0, root.pages.value.selectedIndex)
        // Экран и его viewModel не пересоздаются при повторном open.
        assertSame(leaf0Before, root.pages.value.leaf(0))
        assertSame(vm0Before, root.pages.value.leaf(0).vm)
    }

    @Test
    fun inactiveTabRetainsViewModelWhenKeepInactive() = runTest {
        setMain()
        val root = mountPages(keepInactive = true)
        val vm0 = root.pages.value.leaf(0).vm

        root.open(LeafParams(1)) // делает вкладку 0 неактивной
        root.open(LeafParams(0)) // возвращаемся

        assertSame(vm0, root.pages.value.leaf(0).vm)
        assertTrue(vm0.isActive)
    }

    @Test
    fun selectedTabIsResumedInactiveIsCreated() = runTest {
        setMain()
        val root = mountPages(keepInactive = true)
        root.open(LeafParams(1))

        assertEquals(Lifecycle.State.CREATED, root.pages.value.leaf(0).lifecycleState)
        assertEquals(Lifecycle.State.RESUMED, root.pages.value.leaf(1).lifecycleState)
    }

    @Test
    fun closeSelectedLastTabSelectsPrevious() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))
        root.open(LeafParams(2))
        assertEquals(2, root.pages.value.selectedIndex)

        root.close(LeafParams(2))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
        assertEquals(1, root.pages.value.selectedIndex)
    }

    @Test
    fun closeTabBeforeSelectedKeepsSelectionOnSameScreen() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))
        root.open(LeafParams(2)) // selected = 2 (LeafParams(2))

        root.close(LeafParams(0)) // удаляем вкладку до выбранной

        assertEquals(listOf(LeafParams(1), LeafParams(2)), root.pages.value.paramsList)
        // Выбранной остаётся LeafParams(2), но её индекс сместился на 1.
        assertEquals(1, root.pages.value.selectedIndex)
        assertEquals(LeafParams(2), root.pages.value.paramsList[root.pages.value.selectedIndex])
    }

    @Test
    fun closeDestroysViewModel() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))
        val vm1 = root.pages.value.leaf(1).vm
        assertTrue(vm1.isActive)

        root.close(LeafParams(1))

        assertEquals(listOf(LeafParams(0)), root.pages.value.paramsList)
        assertFalse(vm1.isActive)
    }

    @Test
    fun closeNonExistentScreenIsNoOp() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))

        root.close(LeafParams(99))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
        assertEquals(1, root.pages.value.selectedIndex)
    }

    @Test
    fun reopeningClosedScreenCreatesFreshInstance() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))
        val vm1First = root.pages.value.leaf(1).vm

        root.close(LeafParams(1))
        root.open(LeafParams(1))

        val vm1Second = root.pages.value.leaf(1).vm
        assertTrue(vm1Second !== vm1First)
        assertTrue(vm1Second.isActive)
        assertFalse(vm1First.isActive)
    }

    @Test
    fun intentDeliveredToNewlyOpenedScreen() = runTest {
        setMain()
        val root = mountPages()

        root.openWithIntent(IntentLeafParams(5), TestLeafIntent(42))
        testScheduler.advanceUntilIdle()

        val screen = root.pages.value.items
            .first { it.configuration.screenParams == IntentLeafParams(5) }
            .instance as IntentLeafScreen
        assertEquals(listOf(TestLeafIntent(42)), screen.receivedIntents)
    }

    @Test
    fun intentDeliveredToExistingScreenOnReopen() = runTest {
        setMain()
        val root = mountPages()
        root.openWithIntent(IntentLeafParams(5), TestLeafIntent(1))
        testScheduler.advanceUntilIdle()
        val screen = root.pages.value.items
            .first { it.configuration.screenParams == IntentLeafParams(5) }
            .instance as IntentLeafScreen

        root.openWithIntent(IntentLeafParams(5), TestLeafIntent(2))
        testScheduler.advanceUntilIdle()

        // Тот же инстанс, оба интента доставлены.
        assertSame(
            screen,
            root.pages.value.items
                .first { it.configuration.screenParams == IntentLeafParams(5) }
                .instance,
        )
        assertEquals(listOf(TestLeafIntent(1), TestLeafIntent(2)), screen.receivedIntents)
    }

    @Test
    fun closeSelfClosesOwnTab() = runTest {
        setMain()
        val root = mountPages()
        root.open(LeafParams(1))
        val leaf1 = root.pages.value.leaf(1)

        leaf1.closeSelf()

        assertEquals(listOf(LeafParams(0)), root.pages.value.paramsList)
        assertFalse(leaf1.vm.isActive)
    }
}
