package ru.vs.core.navigation.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import ru.vs.core.decompose.ui.LocalComponentContextHolder
import ru.vs.core.navigation.Screen

class ScreenInstance(private val screen: Screen, private val componentContext: ComponentContext) {
    @Composable
    internal fun ScreenInstanceView() {
        LocalComponentContextHolder(componentContext) {
            screen.ScreenView()
        }
    }
}
