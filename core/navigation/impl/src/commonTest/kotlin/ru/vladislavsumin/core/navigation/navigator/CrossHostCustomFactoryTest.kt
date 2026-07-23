package ru.vladislavsumin.core.navigation.navigator

import kotlinx.coroutines.test.runTest
import ru.vladislavsumin.core.coroutines.test.setMain
import ru.vladislavsumin.core.navigation.testData.CrossRootScreen
import ru.vladislavsumin.core.navigation.testData.LeafParams
import ru.vladislavsumin.core.navigation.testData.NavigationIntegrationTestBase
import ru.vladislavsumin.core.navigation.testData.crossHostNavigation
import ru.vladislavsumin.core.navigation.testData.paramsList
import kotlin.test.Test
import kotlin.test.assertEquals

class CrossHostCustomFactoryTest : NavigationIntegrationTestBase() {

    private fun mountRoot(): CrossRootScreen = mount(crossHostNavigation())

    @Test
    fun openLeafWithCustomFactoryFromMiddle() = runTest {
        setMain()
        val root = mountRoot()
        val middle = root.middle(0)

        middle.openLeafWithCustomFactory(LeafParams(1))

        assertEquals(listOf(LeafParams(0), LeafParams(1)), middle.stack.value.paramsList)
    }

    @Test
    fun openLeafNormalInMiddleUsesRegisteredFactory() = runTest {
        setMain()
        val root = mountRoot()
        val middle = root.middle(0)

        middle.openLeaf(LeafParams(2))

        assertEquals(listOf(LeafParams(0), LeafParams(2)), middle.stack.value.paramsList)
    }

    @Test
    fun openLeafWithCustomFactoryPreservesInitialLeaf() = runTest {
        setMain()
        val root = mountRoot()
        val middle = root.middle(0)

        middle.openLeafWithCustomFactory(LeafParams(5))
        middle.openLeafWithCustomFactory(LeafParams(10))

        assertEquals(listOf(LeafParams(0), LeafParams(5), LeafParams(10)), middle.stack.value.paramsList)
    }
}
