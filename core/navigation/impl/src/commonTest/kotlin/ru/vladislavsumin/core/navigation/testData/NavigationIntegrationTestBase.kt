package ru.vladislavsumin.core.navigation.testData

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.errorhandler.onDecomposeError
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.host.childNavigationRoot
import ru.vladislavsumin.core.navigation.screen.GenericScreen

/**
 * Базовый класс для end-to-end характеризационных тестов навигации.
 *
 * Поднимает реальный граф навигации на управляемом [LifecycleRegistry] и предоставляет доступ к корневому экрану.
 * Тесты должны оборачиваться в `runTest { setMain(); ... }`, так как компоненты используют `Dispatchers.Main`.
 */
abstract class NavigationIntegrationTestBase {

    @Suppress("DEPRECATION")
    private val decomposeErrorHandler = run { onDecomposeError = {} }

    val lifecycle: LifecycleRegistry = LifecycleRegistry()
    val context: DefaultComponentContext = DefaultComponentContext(lifecycle)

    init {
        LoggerManager.initTest()
        CountingViewModel.nextId = 0
    }

    /**
     * Монтирует переданный граф навигации и возвращает корневой экран нужного типа. После монтирования переводит
     * lifecycle в RESUMED.
     */
    inline fun <reified T : GenericScreen<*>> mount(navigation: GenericNavigation<*>): T {
        @Suppress("UNCHECKED_CAST")
        val typedNavigation = navigation as GenericNavigation<ComponentContext>
        val root = context.childNavigationRoot(typedNavigation)
        lifecycle.resume()
        return root as T
    }
}
