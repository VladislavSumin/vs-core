package ru.vladislavsumin.core.navigation.repository

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.NavigationHostB
import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import ru.vladislavsumin.core.navigation.testData.ScreenC
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class NavigationRepositoryTest {
    @Test
    fun checkNoneScreenRegistration() {
        val repository = NavigationRepositoryImpl<ComponentContext>(emptySet())
        assertEquals(0, repository.screens.size)
        assertEquals(0, repository.serializers.size)
    }

    @Test
    fun checkSingleScreenRegistration() {
        val registrars = setOf(
            NavigationRegistrar { registerScreen(factory = FailingScreenFactory<ScreenA>()) },
        )
        val repository = NavigationRepositoryImpl(registrars)
        assertEquals(1, repository.screens.size)
        assertEquals(1, repository.serializers.size)
        assertEquals(repository.screens.keys.first(), ScreenA.asKey())
    }

    @Test
    fun checkMultipleScreenRegistration() {
        val registrars = setOf(
            GenericNavigationRegistrar { registerScreen(factory = FailingScreenFactory<ScreenA>()) },
            GenericNavigationRegistrar { registerScreen(factory = FailingScreenFactory<ScreenB>()) },
        )
        val repository = NavigationRepositoryImpl(registrars)
        assertEquals(2, repository.screens.size)
        assertEquals(2, repository.serializers.size)
    }

    @Test
    fun checkDoubleScreenRegistration() {
        val registrars = setOf(
            GenericNavigationRegistrar { registerScreen(factory = FailingScreenFactory<ScreenA>()) },
            GenericNavigationRegistrar { registerScreen(factory = FailingScreenFactory<ScreenA>()) },
        )
        assertFailsWith<DoubleScreenRegistrationException> {
            NavigationRepositoryImpl(registrars)
        }
    }

    @Test
    fun checkScreenRegistrationAfterFinalize() {
        var screenRegistry: NavigationRegistry<ComponentContext>? = null
        val registrars = setOf(
            GenericNavigationRegistrar { screenRegistry = this },
        )
        NavigationRepositoryImpl(registrars)

        assertFailsWith<ScreenRegistrationAfterFinalizeException> {
            screenRegistry!!.registerScreen(factory = FailingScreenFactory<ScreenA>())
        }
    }

    @Test
    fun checkDoubleHostRegistration() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                        NavigationHostA opens setOf(ScreenC::class)
                    },
                )
                registerScreen(factory = FailingScreenFactory<ScreenB>())
                registerScreen(factory = FailingScreenFactory<ScreenC>())
            },
        )
        assertFailsWith<DoubleHostRegistrationException> {
            NavigationRepositoryImpl(registrars)
        }
    }

    @Test
    fun checkMultipleScreenRegistrationInSameParent() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                        NavigationHostB opens setOf(ScreenB::class)
                    },
                )
                registerScreen(factory = FailingScreenFactory<ScreenB>())
            },
        )
        assertFailsWith<MultipleScreenRegistrationInSameParentException> {
            NavigationRepositoryImpl(registrars)
        }
    }

    @Test
    fun checkScreenWithNavigationHosts() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(ScreenB::class)
                    },
                )
                registerScreen(factory = FailingScreenFactory<ScreenB>())
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        assertEquals(2, repository.screens.size)
        val screenARegistration = repository.screens[ScreenA.asKey()]
        assertNotNull(screenARegistration)
        assertEquals(1, screenARegistration.navigationHosts.size)
    }

    @Test
    fun checkScreenWithDefaultParams() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    defaultParams = ScreenA,
                    factory = FailingScreenFactory(),
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        assertEquals(1, repository.screens.size)
        val registration = repository.screens[ScreenA.asKey()]
        assertNotNull(registration)
        assertEquals(ScreenA, registration.defaultParams)
    }

    @Test
    fun checkScreenWithDescription() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen(
                    factory = FailingScreenFactory<ScreenA>(),
                    description = "Test screen",
                )
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val registration = repository.screens[ScreenA.asKey()]
        assertNotNull(registration)
        assertEquals("Test screen", registration.description)
    }

    @Test
    fun checkScreenWithoutFactory() {
        val registrars = setOf(
            NavigationRegistrar {
                registerScreen<ScreenA>()
            },
        )
        val repository = NavigationRepositoryImpl(registrars)
        val registration = repository.screens[ScreenA.asKey()]
        assertNotNull(registration)
        assertEquals(null, registration.factory)
    }
}
