package ru.vladislavsumin.core.navigation

import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
