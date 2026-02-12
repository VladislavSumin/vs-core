package ru.vladislavsumin.core.navigation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ScreenTest {
    @Test
    fun testWrongScreenCreation() {
        assertFailsWith<IllegalStateException> {
            WrongScreen(DefaultComponentContext(LifecycleRegistry()))
        }
    }
}

private class WrongScreen(context: ComponentContext) : Screen(context) {
    @Composable
    override fun Render(modifier: Modifier) {
        // empty
    }
}
