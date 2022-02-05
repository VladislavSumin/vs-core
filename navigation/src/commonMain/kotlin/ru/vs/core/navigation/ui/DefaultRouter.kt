package ru.vs.core.navigation.ui

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.Router
import com.arkivanov.decompose.router.router
import ru.vs.core.navigation.Screen

fun ComponentContext.defaultRouter(
    initialScreen: Screen,
    key: String = "defaultRouter"
): Router<Screen, ScreenInstance> {
    return this.router(
        initialConfiguration = initialScreen,
        key = key,
        handleBackButton = true,
        childFactory = ::createChild
    )
}

@Suppress("UnusedPrivateMember")
private fun createChild(config: Screen, componentContext: ComponentContext) = ScreenInstance(config, componentContext)
