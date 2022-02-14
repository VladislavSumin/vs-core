package ru.vs.core.navigation.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.ChildAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.crossfade
import ru.vs.core.navigation.Screen

@Composable
fun NavigationContentView(animation: ChildAnimation<Screen, ScreenInstance> = crossfade()) {
    val routerState = LocalNavigation.current.state
    Children(
        routerState = routerState,
        animation = animation,
    ) { childCreated ->
        childCreated.instance.ScreenInstanceView()
    }
}
