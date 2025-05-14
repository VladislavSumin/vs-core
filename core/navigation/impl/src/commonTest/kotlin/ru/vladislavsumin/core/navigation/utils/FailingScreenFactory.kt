package ru.vladislavsumin.core.navigation.utils

import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

class FailingScreenFactory<P : ScreenParams> : ScreenFactory<P, Screen> {
    override fun create(context: ScreenContext, params: P): Screen {
        error("Fail")
    }
}
