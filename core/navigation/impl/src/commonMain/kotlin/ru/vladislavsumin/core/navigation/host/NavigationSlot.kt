package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.navigate
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.HostNavigator
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.asKey

/**
 * Навигация типа "слот", означает, что в ней одновременно может быть только один экран. Пред идущий экран при этом
 * полностью закрывается.
 *
 * @param navigationHost навигационный хост для возможности понять, какие экраны будут открываться в этой навигации.
 * @param initialConfiguration начальный экран, который будет открыт в данной навигации. Можно использовать null, если
 * в дальнейшем мы будем открывать тут экраны через навигационный граф.
 * @param key уникальный в пределах экрана ключ для навигации.
 * @param closeParentWhenEmpty закрывает родительский экран при попытке закрыть экран в этом слоте.
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 * @param allowStateSave разрешает сохранять состояние экранов открытых в данном навигаторе.
 */
public fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.childNavigationSlot(
    navigationHost: NavigationHost,
    initialConfiguration: () -> IntentScreenParams<*>? = { null },
    key: String = "slot_navigation",
    closeParentWhenEmpty: Boolean = false,
    extraLifecycle: Lifecycle? = null,
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildSlot<ConfigurationHolder, GenericScreen<Ctx>>> {
    val source = SlotNavigation<ConfigurationHolder>()

    val hostNavigator = SlotHostNavigator(source) {
        if (closeParentWhenEmpty) {
            internalNavigator.close()
            false
        } else {
            true
        }
    }
    internalNavigator.registerHostNavigator(navigationHost, hostNavigator)

    val context = if (extraLifecycle != null) internalContext.childContext(key, extraLifecycle) else internalContext

    val slot = context.childSlot(
        source = source,
        saveConfiguration = { state ->
            if (allowStateSave && state != null) {
                SerializableContainer(value = state.screenParams, strategy = internalNavigator.serializer)
            } else {
                null
            }
        },
        restoreConfiguration = { container ->
            val screenParams = container.consumeRequired(strategy = internalNavigator.serializer)
            ConfigurationHolder(screenParams)
        },
        key = key,
        initialConfiguration = {
            val initial = internalNavigator.getInitialParamsFor(navigationHost)
            if (initial != null) {
                ConfigurationHolder(initial.screenParams, initial.intent)
            } else {
                initialConfiguration()?.let { ConfigurationHolder(it) }
            }
        },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )

    return slot
}

private class SlotHostNavigator(
    private val slotNavigation: SlotNavigation<ConfigurationHolder>,
    private val allowCloseScreen: () -> Boolean,
) : HostNavigator {
    override fun open(params: IntentScreenParams<*>, intent: ScreenIntent?) {
        // Просто открываем переданный экран, логика слот навигации закроет предыдущий экран если он другой
        // или не будет делать ничего если уже открыт искомый экран.
        slotNavigation.navigate { currentOpenedScreen ->
            val newConfig = if (currentOpenedScreen != null && currentOpenedScreen.screenParams == params) {
                currentOpenedScreen
            } else {
                ConfigurationHolder(params)
            }
            newConfig.sendIntent(intent)
            newConfig
        }
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<*>) {
        // Проверяем, если текущий экран имеет такой же ключ, то оставляем его, иначе заменяем на defaultParams
        slotNavigation.navigate { currentOpenedScreen ->
            if (currentOpenedScreen != null && currentOpenedScreen.screenParams.asKey() == screenKey) {
                currentOpenedScreen
            } else {
                ConfigurationHolder(defaultParams())
            }
        }
    }

    override fun close(params: IntentScreenParams<*>): Boolean {
        var isSuccess: Boolean? = null
        slotNavigation.navigate { currentOpenedScreen ->
            if (params == currentOpenedScreen?.screenParams) {
                isSuccess = true
                if (allowCloseScreen()) null else currentOpenedScreen
            } else {
                isSuccess = false
                currentOpenedScreen
            }
        }
        return isSuccess ?: error("unreachable")
    }

    override fun close(screenKey: ScreenKey): Boolean {
        var isSuccess: Boolean? = null
        slotNavigation.navigate { currentOpenedScreen ->
            when {
                // Все экраны закрыты
                currentOpenedScreen == null -> {
                    isSuccess = false
                    currentOpenedScreen
                }

                // Открыт нужный нам экран
                screenKey == currentOpenedScreen.screenParams.asKey() -> {
                    isSuccess = true
                    if (allowCloseScreen()) null else currentOpenedScreen
                }

                // Открыт другой экран, закрывать нечего.
                else -> {
                    isSuccess = false
                    currentOpenedScreen
                }
            }
        }
        return isSuccess ?: error("unreachable")
    }
}
