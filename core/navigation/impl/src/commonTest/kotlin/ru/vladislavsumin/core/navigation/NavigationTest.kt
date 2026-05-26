package ru.vladislavsumin.core.navigation

import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class NavigationTest {
    @Test
    fun testFindDefaultScreenParamsByName() {
        val registrars = setOf(
            GenericNavigationRegistrar {
                registerScreen(
                    defaultParams = ScreenA,
                    factory = FailingScreenFactory(),
                )
            },
        )
        val navigation = Navigation(registrars)
        assertEquals(ScreenA, navigation.findDefaultScreenParamsByName("ScreenA"))
    }

    @Test
    fun testFindDefaultScreenParamsByNameNull() {
        val registrars = setOf(
            GenericNavigationRegistrar { registerScreen(factory = FailingScreenFactory<ScreenA>()) },
        )
        val navigation = Navigation(registrars)
        assertNull(navigation.findDefaultScreenParamsByName("ScreenA"))
    }

    @Test
    fun testFindDefaultScreenParamsByNameNotFound() {
        val registrars = setOf(
            GenericNavigationRegistrar {
                registerScreen(
                    defaultParams = ScreenA,
                    factory = FailingScreenFactory(),
                )
            },
        )
        val navigation = Navigation(registrars)
        assertNull(navigation.findDefaultScreenParamsByName("NonExistent"))
    }

    @Test
    fun testOpenSendsEventToChannel() {
        val registrars = setOf(
            GenericNavigationRegistrar {
                registerScreen(
                    defaultParams = ScreenA,
                    factory = FailingScreenFactory(),
                    navigationHosts = {
                        ru.vladislavsumin.core.navigation.testData.NavigationHostA opens setOf(ScreenB::class)
                    },
                )
                registerScreen(
                    factory = FailingScreenFactory<ScreenB>(),
                )
            },
        )
        val navigation = Navigation(registrars)
        navigation.open(ScreenA)
        val event = navigation.navigationChannel.tryReceive().getOrNull()
        assertIs<GenericNavigation.NavigationEvent.Open>(event)
        assertEquals(ScreenA, event.screenParams)
    }
}
