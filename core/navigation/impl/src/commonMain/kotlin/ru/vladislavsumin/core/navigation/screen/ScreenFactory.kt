package ru.vladislavsumin.core.navigation.screen

import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

/**
 * Фабрика для создания компонента экрана.
 * @param P тип параметров экрана.
 * @param S тип экрана.
 */
public fun interface ScreenFactory<P : IntentScreenParams<I>, I : ScreenIntent, S : Screen> {
    /**
     * Создает компонент экрана.
     * @param context контекст экрана.
     * @param params параметры экрана.
     * @param intents события экрана.
     */
    public fun create(context: ScreenContext, params: P, intents: ReceiveChannel<I>): S
}
