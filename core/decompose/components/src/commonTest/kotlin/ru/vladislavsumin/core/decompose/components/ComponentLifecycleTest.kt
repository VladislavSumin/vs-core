package ru.vladislavsumin.core.decompose.components

import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.decompose.test.BaseComponentTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ComponentLifecycleTest : BaseComponentTest() {
    @Test
    fun testResubscribeDoesNotEmitBelowLifecycle() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.resubscribed.collect { received += it } }
        runCurrent()

        assertEquals(emptyList(), received)
    }

    @Test
    fun testResubscribeEmitsCurrentAfterResume() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.resubscribed.collect { received += it } }
        runCurrent()

        context.lifecycleRegistry.resume()
        runCurrent()

        assertEquals(listOf(0), received)
    }

    @Test
    fun testResubscribeReceivesUpdatesWhileActive() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.resubscribed.collect { received += it } }
        runCurrent()

        context.lifecycleRegistry.resume()
        runCurrent()
        component.source.value = 1
        runCurrent()

        assertEquals(listOf(0, 1), received)
    }

    @Test
    fun testResubscribeStopsReceivingBelowLifecycleAndResubscribes() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.resubscribed.collect { received += it } }
        runCurrent()

        context.lifecycleRegistry.resume()
        runCurrent()
        component.source.value = 1
        runCurrent()

        context.lifecycleRegistry.stop()
        runCurrent()
        component.source.value = 2
        runCurrent()
        assertEquals(listOf(0, 1), received)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(listOf(0, 1, 2), received)
    }

    @Test
    fun testRelaunchRunsOnResume() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        runCurrent()
        assertEquals(0, component.relaunchStartCount.value)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, component.relaunchStartCount.value)
    }

    @Test
    fun testRelaunchCancelsAndRerunsOnLifecycle() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, component.relaunchStartCount.value)
        assertEquals(0, component.relaunchCancelCount.value)

        context.lifecycleRegistry.stop()
        runCurrent()
        assertEquals(1, component.relaunchCancelCount.value)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(2, component.relaunchStartCount.value)
    }

    @Test
    fun testRelaunchStopsOnDestroy() = runTest {
        setMain()
        val component = LifecycleTestComponent(context)
        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, component.relaunchStartCount.value)

        context.lifecycleRegistry.destroy()
        runCurrent()
        assertEquals(1, component.relaunchCancelCount.value)
    }
}
