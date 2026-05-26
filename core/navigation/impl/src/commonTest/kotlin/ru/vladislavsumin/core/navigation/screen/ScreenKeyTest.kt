package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.testData.ScreenA
import ru.vladislavsumin.core.navigation.testData.ScreenB
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ScreenKeyTest {
    @Test
    fun testAsKeyReturnsSameKeyForSameClass() {
        val key1 = ScreenA.asKey()
        val key2 = ScreenA.asKey()
        assertEquals(key1, key2)
    }

    @Test
    fun testAsKeyReturnsDifferentKeyForDifferentClass() {
        val keyA = ScreenA.asKey()
        val keyB = ScreenB.asKey()
        assertNotEquals(keyA, keyB)
    }

    @Test
    fun testScreenKeySimpleName() {
        val key = ScreenA.asKey()
        assertEquals("ScreenA", key.key.simpleName)
    }
}
