package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Навигатор, который может производить навигацию в пределах одного хоста навигации.
 */
internal interface HostNavigator {
    /**
     * Открывает экран с переданными параметрами, или возвращается в нему в соответствии с правилами навигации,
     * если такой экран уже открыт.
     *
     * @param params параметры экрана.
     */
    fun open(params: IntentScreenParams<ScreenIntent>)

    fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<ScreenIntent>)

    /**
     * Пытается закрыть экран с соответствующими параметрами. Если закрыть экран невозможно по какой-либо причине **не**
     * выбрасывает ошибку, это является штатной ситуацией.
     *
     * @param params параметры экрана.
     * @return true если экран был успешно закрыт, false в других случаях.
     */
    fun close(params: IntentScreenParams<ScreenIntent>): Boolean

    /**
     * Пытается закрыть экран с соответствующим ключом согласно внутренним правилам навигации. Если навигация содержит
     * более одного активного экрана соответствующего ключу, то закрыт будет один из них (в зависимости от внутреннего
     * устройства навигации). Если закрыть экран невозможно по какой-либо причине **не** выбрасывает ошибку, это
     * является штатной ситуацией.
     *
     * @param screenKey ключ экрана.
     * @return true если экран был успешно закрыт, false в других случаях.
     */
    fun close(screenKey: ScreenKey): Boolean
}
