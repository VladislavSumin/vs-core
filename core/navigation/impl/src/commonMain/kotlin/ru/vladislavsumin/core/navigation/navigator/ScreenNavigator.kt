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
     * @param hints - необязательный список подсказок: параметров экранов, которые должны встретиться среди предков
     * открываемого экрана в виде упорядоченной подпоследовательности (в том же порядке, но не обязательно все и с
     * возможными разрывами). Позволяет снять неоднозначность когда экран зарегистрирован в нескольких местах графа,
     * а так же закрепить конкретные инстансы родительских экранов.
     */
    public fun <S : IntentScreenParams<I>, I : ScreenIntent> open(
        screenParams: S,
        intent: I? = null,
        hints: List<IntentScreenParams<*>> = emptyList(),
    )

    /**
     * Закрывает экран соответствующий переданным [screenParams].
     */
    public fun close(screenParams: IntentScreenParams<*>)

    /**
     * Закрывает этот экран.
     */
    public fun close()

    /**
     * Переносит уже открытый экран [screenParams] в другую локацию графа навигации,
     * определяемую с помощью [hints] (и стандартного резолвинга пути).
     *
     * При переносе сохраняется состояние viewModel и Compose-состояние экрана (rememberSaveable).
     * Сам экран пересоздаётся заново — в его конструктор инжектятся свежие зависимости
     * (window-специфичные сервисы), а viewModel переиспользуется из сохранённого instanceKeeper.
     *
     * Для корректной работы экран должен быть открыт в момент переноса,
     * а целевая локация должна отличаться от текущей.
     */
    public fun <S : IntentScreenParams<I>, I : ScreenIntent> transfer(
        screenParams: S,
        hints: List<IntentScreenParams<*>>,
    )
}
