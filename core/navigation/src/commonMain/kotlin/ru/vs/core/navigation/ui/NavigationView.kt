package ru.vs.core.navigation.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.crossfade

@Composable
fun NavigationContentView() {
    val routerState = LocalNavigation.current.state
    Children(
        routerState = routerState,
        animation = crossfade(),
    ) { childCreated ->
        childCreated.instance.ScreenInstanceView()
    }
}
