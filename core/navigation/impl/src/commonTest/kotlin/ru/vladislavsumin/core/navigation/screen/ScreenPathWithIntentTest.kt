package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.testData.ScreenA
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScreenPathWithIntentTest {
    @Test
    fun testScreenPathWithIntentWithNullIntent() {
        val path = ScreenPath(ScreenA)
        val pathWithIntent = ScreenPathWithIntent(path, null)
        assertEquals(path, pathWithIntent.screenPath)
        assertNull(pathWithIntent.intent)
    }
}
