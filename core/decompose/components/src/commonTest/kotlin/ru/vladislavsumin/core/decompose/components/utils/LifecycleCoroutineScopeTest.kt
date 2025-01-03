package ru.vladislavsumin.core.decompose.components.utils

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LifecycleCoroutineScopeTest {
    @Test
    fun testScopeCancellation() = runTest {
        setMain()
        val lifecycle = LifecycleRegistry()
        lifecycle.create()

        val scope = lifecycle.createCoroutineScope()
        assertTrue(scope.isActive)

        lifecycle.destroy()
        assertFalse(scope.isActive)
    }

    @Test
    fun testCreateScopeOnDestroyedLifecycle() = runTest {
        setMain()
        val lifecycle = LifecycleRegistry()
        lifecycle.create()
        lifecycle.destroy()

        val scope = lifecycle.createCoroutineScope()
        assertFalse(scope.isActive)
    }
}

// TODO Sumin: вынести отдельным тестовым модулем
/**
 * Устанавливает dispatcher [TestScope] в качестве Main dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.setMain() {
    val dispatcher = coroutineContext[ContinuationInterceptor.Key]!! as CoroutineDispatcher
    Dispatchers.setMain(dispatcher)
}
