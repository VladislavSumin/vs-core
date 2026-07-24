package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

/**
 * Навигатор, который может производить навигацию в пределах одного хоста навигации.
 */
internal interface HostNavigator {
    /**
     * Открывает экран с переданными параметрами, или возвращается в нему в соответствии с правилами навигации,
     * если такой экран уже открыт.
     *
     * @param params параметры экрана.
     * @param savedInstance усыновляемый инстанс экрана (при переносе).
     */
    fun open(
        params: IntentScreenParams<*>,
        intent: ScreenIntent?,
        savedInstance: TransferableScreenHolder<*>? = null,
        providerParams: IntentScreenParams<*>? = null,
    )

    fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<*>)

    /**
     * Пытается закрыть экран с соответствующими параметрами. Если закрыть экран невозможно по какой-либо причине **не**
     * выбрасывает ошибку, это является штатной ситуацией.
     *
     * @param params параметры экрана.
     * @return true если экран был успешно закрыт, false в других случаях.
     */
    fun close(params: IntentScreenParams<*>): Boolean

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

    /**
     * Возвращает параметры активного экрана с заданным [screenKey] в этом хосте.
     * Используется для однозначного поиска навигатора после открытия через [open] по ключу,
     * когда в хосте несколько инстансов одного типа.
     *
     * @return параметры активного экрана или `null`, если экран с таким ключом не активен.
     */
    fun getActiveParams(screenKey: ScreenKey): IntentScreenParams<*>?
}
