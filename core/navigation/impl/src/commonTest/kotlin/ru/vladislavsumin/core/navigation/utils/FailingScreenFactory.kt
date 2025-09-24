package ru.vladislavsumin.core.navigation.utils

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.NoIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory

class FailingScreenFactory<P : ScreenParams> :
    ScreenFactory<ComponentContext, P, NoIntent, TestRender, GenericScreen<ComponentContext, TestRender>> {
    override fun create(
        context: ComponentContext,
        params: P,
        intents: ReceiveChannel<NoIntent>,
    ): GenericScreen<ComponentContext, TestRender> {
        error("Fail")
    }
}
