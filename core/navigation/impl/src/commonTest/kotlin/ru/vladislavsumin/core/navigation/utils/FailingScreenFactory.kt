package ru.vladislavsumin.core.navigation.utils

import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

class FailingScreenFactory<P : ScreenParams> : ScreenFactory<P, NoIntent, Screen> {
    override fun create(context: ScreenContext, params: P, intents: ReceiveChannel<NoIntent>): Screen {
        error("Fail")
    }
}
