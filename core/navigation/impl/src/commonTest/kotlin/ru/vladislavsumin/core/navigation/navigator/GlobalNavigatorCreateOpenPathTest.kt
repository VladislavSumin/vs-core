package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.registration.GenericNavigationRegistrar
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.ScreenPath.PathElement
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.testData.HintBranchLeft
import ru.vladislavsumin.core.navigation.testData.HintBranchRight
import ru.vladislavsumin.core.navigation.testData.HintMiddle
import ru.vladislavsumin.core.navigation.testData.HintRoot
import ru.vladislavsumin.core.navigation.testData.HintTarget
import ru.vladislavsumin.core.navigation.testData.NavigationHostA
import ru.vladislavsumin.core.navigation.testData.NavigationHostB
import ru.vladislavsumin.core.navigation.utils.FailingScreenFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Тесты поиска пути до экрана с учетом подсказок ([GlobalNavigator.createOpenPath]).
 *
 * Тестовый граф (целевой экран [HintTarget] зарегистрирован в двух местах):
 * ```
 * HintRoot
 *   ├─ HintBranchLeft ─ HintTarget            (глубина 2)
 *   └─ HintBranchRight ─ HintMiddle ─ HintTarget (глубина 3)
 * ```
 */
class GlobalNavigatorCreateOpenPathTest {

    private fun navigation(): GenericNavigation<ComponentContext> = Navigation(
        setOf(
            GenericNavigationRegistrar {
                registerScreen(
                    defaultParams = HintRoot,
                    factory = FailingScreenFactory<HintRoot>(),
                    navigationHosts = {
                        NavigationHostA opens setOf(HintBranchLeft::class)
                        NavigationHostB opens setOf(HintBranchRight::class)
                    },
                )
                registerScreen(
                    factory = FailingScreenFactory<HintBranchLeft>(),
                    navigationHosts = { NavigationHostA opens setOf(HintTarget::class) },
                )
                registerScreen(
                    factory = FailingScreenFactory<HintBranchRight>(),
                    navigationHosts = { NavigationHostA opens setOf(HintMiddle::class) },
                )
                registerScreen(
                    factory = FailingScreenFactory<HintMiddle>(),
                    navigationHosts = { NavigationHostA opens setOf(HintTarget::class) },
                )
                registerScreen(factory = FailingScreenFactory<HintTarget>())
            },
        ),
    )

    private fun globalNavigator() = GlobalNavigator(navigation())

    @Test
    fun testNoHintsResolvesNearestBranch() {
        val path = globalNavigator().createOpenPath(
            startScreenPath = ScreenPath(HintRoot),
            targetScreenParams = HintTarget,
            hints = emptyList(),
        )
        assertEquals(
            ScreenPath(
                listOf(
                    PathElement.Key(HintBranchLeft.asKey()),
                    PathElement.Params(HintTarget),
                ),
            ),
            path,
        )
    }

    @Test
    fun testHintForcesFarBranch() {
        val path = globalNavigator().createOpenPath(
            startScreenPath = ScreenPath(HintRoot),
            targetScreenParams = HintTarget,
            hints = listOf(HintMiddle(id = 1)),
        )
        assertEquals(
            ScreenPath(
                listOf(
                    PathElement.Key(HintBranchRight.asKey()),
                    PathElement.Params(HintMiddle(id = 1)),
                    PathElement.Params(HintTarget),
                ),
            ),
            path,
        )
    }

    @Test
    fun testHintMatchesAsSubsequenceWithGap() {
        val path = globalNavigator().createOpenPath(
            startScreenPath = ScreenPath(HintRoot),
            targetScreenParams = HintTarget,
            // Подсказка указывает только на HintBranchRight, между ней и целью есть разрыв (HintMiddle).
            hints = listOf(HintBranchRight),
        )
        assertEquals(
            ScreenPath(
                listOf(
                    PathElement.Params(HintBranchRight),
                    PathElement.Key(ScreenKey(HintMiddle::class)),
                    PathElement.Params(HintTarget),
                ),
            ),
            path,
        )
    }

    @Test
    fun testHintInstanceIsPinnedInPath() {
        val path = globalNavigator().createOpenPath(
            startScreenPath = ScreenPath(HintRoot),
            targetScreenParams = HintTarget,
            hints = listOf(HintMiddle(id = 42)),
        )
        assertEquals(PathElement.Params(HintMiddle(id = 42)), path[1])
    }

    @Test
    fun testHintsInWrongOrderThrows() {
        assertFailsWith<NoScreenMatchingHintsException> {
            globalNavigator().createOpenPath(
                startScreenPath = ScreenPath(HintRoot),
                targetScreenParams = HintTarget,
                hints = listOf(HintMiddle(id = 1), HintBranchRight),
            )
        }
    }

    @Test
    fun testUnsatisfiableHintsThrows() {
        assertFailsWith<NoScreenMatchingHintsException> {
            globalNavigator().createOpenPath(
                startScreenPath = ScreenPath(HintRoot),
                targetScreenParams = HintTarget,
                // Обе подсказки существуют в графе, но не в одном пути до цели.
                hints = listOf(HintBranchLeft, HintMiddle(id = 1)),
            )
        }
    }
}
