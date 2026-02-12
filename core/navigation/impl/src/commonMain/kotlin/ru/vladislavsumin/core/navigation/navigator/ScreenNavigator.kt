package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

public interface ScreenNavigator {
    /**
     * Открывает экран соответствующий переданным [screenParams], при этом, при поиске места открытия экрана учитывается
     * текущее место. (подробнее про приоритет выбора места написано в документации).
     *
     * @param intent - опциональное событие экрана. При передаче (впрочем без события поведение будет таким же) экран
     * не открывается повторно если уже открыт, но получает новое событие. Если экран был закрыт, то он откроется и
     * сразу получить новое событие.
     */
    public fun <S : IntentScreenParams<I>, I : ScreenIntent> open(screenParams: S, intent: I? = null)

    /**
     * Закрывает экран соответствующий переданным [screenParams].
     */
    public fun close(screenParams: IntentScreenParams<*>)

    /**
     * Закрывает этот экран.
     */
    public fun close()
}
