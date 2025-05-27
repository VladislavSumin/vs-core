package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams

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
     */
    public fun create(context: ScreenContext, params: P): S
}
