package ru.vladislavsumin.core.navigation.screen

import com.arkivanov.decompose.GenericComponentContext
import kotlinx.coroutines.channels.ReceiveChannel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

/**
 * Фабрика для создания компонента экрана.
 * @param P тип параметров экрана.
 * @param S тип экрана.
 */
public fun interface ScreenFactory<
    Ctx : GenericComponentContext<Ctx>,
    P : IntentScreenParams<I>,
    I : ScreenIntent,
    BS : GenericScreen<Ctx, BS>,
    S : BS,
    > {
    /**
     * Создает компонент экрана.
     * @param context контекст экрана.
     * @param params параметры экрана.
     * @param intents события экрана.
     */
    public fun create(context: Ctx, params: P, intents: ReceiveChannel<I>): S
}
