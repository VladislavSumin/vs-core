package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.LeafScreen
import ru.vladislavsumin.core.navigation.testData.MiddleParams
import ru.vladislavsumin.core.navigation.testData.MiddleScreen
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.NestedRootScreen
import ru.vladislavsumin.core.navigation.testData.PagesRootScreen
import ru.vladislavsumin.core.navigation.testData.leaf
import ru.vladislavsumin.core.navigation.testData.lifecycleState
import ru.vladislavsumin.core.navigation.testData.nestedNavigation
import ru.vladislavsumin.core.navigation.testData.pagesNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Характеризационные тесты глобального закрытия экранов через [Navigation.close] →
 * канал → [handleNavigation] → [GlobalNavigator.close].
 */
class GlobalNavigatorCloseTest : NavigationIntegrationTestBase() {

    @Test
    fun closeActiveScreenInPagesRemovesIt() = runTest {
        setMain()
        val navigation = pagesNavigation(initial = listOf(LeafParams(0)))
        val root = mount(navigation) as PagesRootScreen
        root.open(LeafParams(1))
        val leaf1 = root.pages.value.leaf(1)

        navigation.close(unsafeCast(LeafParams(1)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(LeafParams(0)), root.pages.value.paramsList)
        assertEquals(Lifecycle.State.DESTROYED, leaf1.lifecycleState)
    }

    @Test
    fun closeScreenClosesSubtreeInNestedStructure() = runTest {
        setMain()
        val navigation = nestedNavigation()
        val root = mount(navigation) as NestedRootScreen
        root.middle(0).open(LeafParams(1))
        val middle0 = root.middle(0)
        val leaf1 = middle0.stack.value.items[1].instance as LeafScreen

        navigation.close(unsafeCast(LeafParams(1)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(LeafParams(0)), root.middle(0).stack.value.paramsList)
        assertEquals(Lifecycle.State.DESTROYED, leaf1.lifecycleState)
    }

    @Test
    fun closeMiddleScreenClosesEntireTabWithSubtree() = runTest {
        setMain()
        val navigation = nestedNavigation()
        val root = mount(navigation) as NestedRootScreen
        root.open(MiddleParams(1))
        val middle0 = root.middle(0)
        val leaf0 = middle0.stack.value.items[0].instance as LeafScreen

        navigation.close(unsafeCast(MiddleParams(0)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(MiddleParams(1)), root.pages.value.paramsList)
        assertEquals(Lifecycle.State.DESTROYED, middle0.lifecycleState)
        assertEquals(Lifecycle.State.DESTROYED, leaf0.lifecycleState)
    }

    @Test
    fun closeNonExistentScreenIsNoOp() = runTest {
        setMain()
        val navigation = pagesNavigation(initial = listOf(LeafParams(0)))
        val root = mount(navigation) as PagesRootScreen
        root.open(LeafParams(1))

        navigation.close(unsafeCast(LeafParams(99)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
    }

    @Test
    fun reopenAfterCloseWorksWithoutErrors() = runTest {
        setMain()
        val navigation = pagesNavigation(initial = listOf(LeafParams(0)))
        val root = mount(navigation) as PagesRootScreen
        root.open(LeafParams(1))

        navigation.close(unsafeCast(LeafParams(1)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(LeafParams(0)), root.pages.value.paramsList)

        root.open(LeafParams(1))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(LeafParams(0), LeafParams(1)), root.pages.value.paramsList)
        val leaf = root.pages.value.leaf(1)
        assertTrue(leaf.vm.isActive)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        private fun unsafeCast(params: IntentScreenParams<*>): IntentScreenParams<ScreenIntent> =
            params as IntentScreenParams<ScreenIntent>

        private val NestedRootScreen.middle: (Int) -> MiddleScreen
            get() = { index -> pages.value.items[index].instance as MiddleScreen }
    }
}
