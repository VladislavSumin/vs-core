package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import ru.vladislavsumin.core.navigation.testData.ScreenC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScreenPathTest {
    @Test
    fun testConstructorFromSingleParams() {
        val path = ScreenPath(ScreenA)
        assertEquals(1, path.size)
        assertTrue(path[0] is ScreenPath.PathElement.Params)
        assertEquals(ScreenA, (path[0] as ScreenPath.PathElement.Params).screenParams)
    }

    @Test
    fun testConstructorFromIterable() {
        val path = ScreenPath(listOf(ScreenA, ScreenB))
        assertEquals(2, path.size)
        assertEquals(ScreenA, (path[0] as ScreenPath.PathElement.Params).screenParams)
        assertEquals(ScreenB, (path[1] as ScreenPath.PathElement.Params).screenParams)
    }

    @Test
    fun testPlusOperator() {
        val path = ScreenPath(ScreenA)
        val newPath = path + ScreenB
        assertEquals(2, newPath.size)
        assertEquals(ScreenA, (newPath[0] as ScreenPath.PathElement.Params).screenParams)
        assertEquals(ScreenB, (newPath[1] as ScreenPath.PathElement.Params).screenParams)
    }

    @Test
    fun testParent() {
        val path = ScreenPath(listOf(ScreenA, ScreenB, ScreenC))
        val parent = path.parent()
        assertEquals(2, parent.size)
        assertEquals(ScreenA, (parent[0] as ScreenPath.PathElement.Params).screenParams)
        assertEquals(ScreenB, (parent[1] as ScreenPath.PathElement.Params).screenParams)
    }

    @Test
    fun testParentOfSingleElement() {
        val path = ScreenPath(ScreenA)
        val parent = path.parent()
        assertEquals(0, parent.size)
    }

    @Test
    fun testReachFromConsistentPath() {
        val pathWithKeys = ScreenPath(
            listOf(
                ScreenPath.PathElement.Key(ScreenA.asKey()),
                ScreenPath.PathElement.Key(ScreenB.asKey()),
            ),
        )
        val otherPath = ScreenPath(listOf(ScreenA, ScreenB))
        val result = pathWithKeys.reachFrom(otherPath)
        assertEquals(2, result.size)
        assertTrue(result[0] is ScreenPath.PathElement.Params)
        assertTrue(result[1] is ScreenPath.PathElement.Params)
        assertEquals(ScreenA, (result[0] as ScreenPath.PathElement.Params).screenParams)
        assertEquals(ScreenB, (result[1] as ScreenPath.PathElement.Params).screenParams)
    }

    @Test
    fun testReachFromDivergingPath() {
        val pathWithKeys = ScreenPath(
            listOf(
                ScreenPath.PathElement.Key(ScreenA.asKey()),
                ScreenPath.PathElement.Key(ScreenB.asKey()),
            ),
        )
        val otherPath = ScreenPath(listOf(ScreenA, ScreenC))
        val result = pathWithKeys.reachFrom(otherPath)
        assertEquals(2, result.size)
        assertTrue(result[0] is ScreenPath.PathElement.Params)
        assertTrue(result[1] is ScreenPath.PathElement.Key)
        assertEquals(ScreenA, (result[0] as ScreenPath.PathElement.Params).screenParams)
        assertEquals(ScreenB.asKey(), (result[1] as ScreenPath.PathElement.Key).screenKey)
    }

    @Test
    fun testReachFromShorterOtherPath() {
        val pathWithKeys = ScreenPath(
            listOf(
                ScreenPath.PathElement.Key(ScreenA.asKey()),
                ScreenPath.PathElement.Key(ScreenB.asKey()),
            ),
        )
        val otherPath = ScreenPath(listOf(ScreenA))
        val result = pathWithKeys.reachFrom(otherPath)
        assertEquals(2, result.size)
        assertTrue(result[0] is ScreenPath.PathElement.Params)
        assertTrue(result[1] is ScreenPath.PathElement.Key)
    }

    @Test
    fun testPathElementKeyAsScreenKey() {
        val key = ScreenA.asKey()
        val element = ScreenPath.PathElement.Key(key)
        assertEquals(key, element.asScreenKey())
    }

    @Test
    fun testPathElementParamsAsScreenKey() {
        val element = ScreenPath.PathElement.Params(ScreenA)
        assertEquals(ScreenA.asKey(), element.asScreenKey())
    }

    @Test
    fun testReachFromParamsElementsPreserved() {
        val pathWithMixed = ScreenPath(
            listOf(
                ScreenPath.PathElement.Params(ScreenA),
                ScreenPath.PathElement.Key(ScreenB.asKey()),
            ),
        )
        val otherPath = ScreenPath(listOf(ScreenA, ScreenB))
        val result = pathWithMixed.reachFrom(otherPath)
        assertTrue(result[0] is ScreenPath.PathElement.Params)
        assertEquals(ScreenA, (result[0] as ScreenPath.PathElement.Params).screenParams)
        assertTrue(result[1] is ScreenPath.PathElement.Params)
        assertEquals(ScreenB, (result[1] as ScreenPath.PathElement.Params).screenParams)
    }
}
