package ru.vladislavsumin.core.navigation.repository

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import ru.vladislavsumin.core.navigation.utils.TestScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NavigationRepositoryTest {
    @Test
    fun checkNoneScreenRegistration() {
        val repository = NavigationRepositoryImpl<ComponentContext, TestScreen>(emptySet())
        assertEquals(0, repository.screens.size)
        assertEquals(0, repository.serializers.size)
    }

    @Test
    fun checkSingleScreenRegistration() {
        val registrars = setOf(
            GenericNavigationRegistrar<ComponentContext, TestScreen> {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        assertEquals(1, repository.screens.size)
        assertEquals(1, repository.serializers.size)
        assertEquals(repository.screens.keys.first(), ScreenA.asKey())
    }

    @Test
    fun checkMultipleScreenRegistration() {
        val registrars = setOf(
            GenericNavigationRegistrar<ComponentContext, TestScreen> {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                )
            },
            GenericNavigationRegistrar<ComponentContext, TestScreen> {
                registerScreen(
                    factory = FailingScreenFactory<ScreenB>(),
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        assertEquals(2, repository.screens.size)
        assertEquals(2, repository.serializers.size)
    }

    @Test
    fun checkDoubleScreenRegistration() {
        val registrars = setOf(
            GenericNavigationRegistrar<ComponentContext, TestScreen> {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                )
            },
            GenericNavigationRegistrar<ComponentContext, TestScreen> {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                )
            },
        )
        assertFailsWith<DoubleScreenRegistrationException> {
            NavigationRepositoryImpl(registrars)
        }
    }

    @Test
    fun checkScreenRegistrationAfterFinalize() {
        var screenRegistry: NavigationRegistry<ComponentContext, TestScreen>? = null
        val registrars = setOf(
            GenericNavigationRegistrar<ComponentContext, TestScreen> { screenRegistry = this },
        )
        NavigationRepositoryImpl(registrars)

        assertFailsWith<ScreenRegistrationAfterFinalizeException> {
            screenRegistry!!.registerScreen(factory = FailingScreenFactory<ScreenA>())
        }
    }
}
