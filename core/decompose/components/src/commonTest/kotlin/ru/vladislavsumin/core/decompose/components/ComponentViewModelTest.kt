package ru.vladislavsumin.core.decompose.components

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.decompose.test.BaseComponentTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ComponentViewModelTest : BaseComponentTest() {
    @Test
    fun testComponentWrongDoubleViewModelCreation() = runTest {
        setMain()

        class TestComponent(context: ComponentContext) : Component(context) {
            val viewModel = viewModel {
                TestViewModel()
                TestViewModel()
            }
        }

        assertFailsWith(WrongViewModelUsageException::class) {
            TestComponent(context)
        }
    }

    @Test
    fun testComponentWrongPlaceToCreateViewModel() = runTest {
        setMain()

        assertFailsWith(WrongViewModelUsageException::class) {
            TestViewModel()
        }
    }
}
