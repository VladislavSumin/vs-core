package ru.vladislavsumin.core.decompose.components

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.decompose.test.BaseComponentTest
import ru.vladislavsumin.core.decompose.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ComponentTest : BaseComponentTest() {
    @Test
    fun testComponentRecreateFull() = runTest {
        setMain()
        val component = TestComponent(context)
        val viewModel = component.testViewModel

        recreateContext()

        val component2 = TestComponent(context)
        val viewModel2 = component2.testViewModel

        assertNotSame(viewModel, viewModel2)
        assertNotEquals(viewModel.testSaveableFlow.value, viewModel2.testSaveableFlow.value)
    }

    @Test
    fun testComponentRecreateProcessDeath() = runTest {
        setMain()
        val component = TestComponent(context)
        val viewModel = component.testViewModel

        recreateContext(RecreateContextType.ProcessDeath)

        val component2 = TestComponent(context)
        val viewModel2 = component2.testViewModel

        assertNotSame(viewModel, viewModel2)
        assertEquals(viewModel.testSaveableFlow.value, viewModel2.testSaveableFlow.value)
    }

    @Test
    fun testComponentRecreateConfigurationChange() = runTest {
        setMain()
        val component = TestComponent(context)
        val viewModel = component.testViewModel

        recreateContext(RecreateContextType.ConfigurationChange)

        val component2 = TestComponent(context)
        val viewModel2 = component2.testViewModel

        assertSame(viewModel, viewModel2)
        assertEquals(viewModel.testSaveableFlow.value, viewModel2.testSaveableFlow.value)
    }
}
