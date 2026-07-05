package ru.vladislavsumin.core.decompose.components

import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.decompose.test.BaseComponentTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ViewModelLifecycleTest : BaseComponentTest() {
    @Test
    fun testResubscribeDoesNotEmitBelowLifecycle() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.viewModel.resubscribed.collect { received += it } }
        runCurrent()

        assertEquals(emptyList(), received)
    }

    @Test
    fun testResubscribeEmitsCurrentAfterResume() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.viewModel.resubscribed.collect { received += it } }
        runCurrent()

        context.lifecycleRegistry.resume()
        runCurrent()

        assertEquals(listOf(0), received)
    }

    @Test
    fun testResubscribeReceivesUpdatesWhileActive() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.viewModel.resubscribed.collect { received += it } }
        runCurrent()

        context.lifecycleRegistry.resume()
        runCurrent()
        component.viewModel.source.value = 1
        runCurrent()

        assertEquals(listOf(0, 1), received)
    }

    @Test
    fun testResubscribeStopsReceivingBelowLifecycleAndResubscribes() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        val received = mutableListOf<Int>()
        backgroundScope.launch { component.viewModel.resubscribed.collect { received += it } }
        runCurrent()

        context.lifecycleRegistry.resume()
        runCurrent()
        component.viewModel.source.value = 1
        runCurrent()

        context.lifecycleRegistry.stop()
        runCurrent()
        component.viewModel.source.value = 2
        runCurrent()
        assertEquals(listOf(0, 1), received)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(listOf(0, 1, 2), received)
    }

    @Test
    fun testRelaunchRunsOnResume() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        runCurrent()
        assertEquals(0, component.viewModel.relaunchStartCount.value)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, component.viewModel.relaunchStartCount.value)
    }

    @Test
    fun testRelaunchCancelsAndRerunsOnLifecycle() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, component.viewModel.relaunchStartCount.value)
        assertEquals(0, component.viewModel.relaunchCancelCount.value)

        context.lifecycleRegistry.stop()
        runCurrent()
        assertEquals(1, component.viewModel.relaunchCancelCount.value)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(2, component.viewModel.relaunchStartCount.value)
    }

    @Test
    fun testViewModelSurvivesConfigurationChange() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        val viewModel = component.viewModel
        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, viewModel.relaunchStartCount.value)

        context.lifecycleRegistry.stop()
        runCurrent()
        assertEquals(1, viewModel.relaunchCancelCount.value)

        recreateContext(RecreateContextType.ConfigurationChange)
        val component2 = LifecycleTestViewModelComponent(context)
        assertSame(viewModel, component2.viewModel)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(2, viewModel.relaunchStartCount.value)
    }

    @Test
    fun testViewModelRecreatedOnProcessDeath() = runTest {
        setMain()
        val component = LifecycleTestViewModelComponent(context)
        val viewModel = component.viewModel
        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, viewModel.relaunchStartCount.value)

        recreateContext(RecreateContextType.ProcessDeath)
        val component2 = LifecycleTestViewModelComponent(context)
        assertNotSame(viewModel, component2.viewModel)

        context.lifecycleRegistry.resume()
        runCurrent()
        assertEquals(1, component2.viewModel.relaunchStartCount.value)
    }
}
