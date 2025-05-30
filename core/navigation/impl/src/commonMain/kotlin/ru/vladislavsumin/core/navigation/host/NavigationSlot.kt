package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.navigate
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.HostNavigator
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
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
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 * @param allowStateSave разрешает сохранять состояние экранов открытых в данном навигаторе.
 */
public fun ScreenContext.childNavigationSlot(
    navigationHost: NavigationHost,
    initialConfiguration: () -> IntentScreenParams<*>? = { null },
    key: String = "slot_navigation",
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildSlot<*, Screen>> {
    val source = SlotNavigation<ConfigurationHolder>()

    val hostNavigator = SlotHostNavigator(source)
    navigator.registerHostNavigator(navigationHost, hostNavigator)

    val slot = childSlot(
        source = source,
        saveConfiguration = { state ->
            if (allowStateSave && state != null) {
                SerializableContainer(value = state.screenParams, strategy = navigator.serializer)
            } else {
                null
            }
        },
        restoreConfiguration = { container ->
            val screenParams = container.consumeRequired(strategy = navigator.serializer)
            ConfigurationHolder(screenParams)
        },
        key = key,
        initialConfiguration = {
            (navigator.getInitialParamsFor(navigationHost) ?: initialConfiguration())
                ?.let { ConfigurationHolder(it) }
        },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )

    return slot
}

private class SlotHostNavigator(
    private val slotNavigation: SlotNavigation<ConfigurationHolder>,
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
            if (intent != null) {
                newConfig.intents.trySend(intent).getOrThrow()
            }
            newConfig
        }
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<ScreenIntent>) {
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
            if (params == currentOpenedScreen) {
                isSuccess = true
                null
            } else {
                isSuccess = false
                currentOpenedScreen
            }
        }
        return isSuccess ?: error("unreachable")
    }

    override fun close(screenKey: ScreenKey): Boolean {
        var isSuccess: Boolean? = null
        slotNavigation.navigate {
            when {
                // Все экраны закрыты
                it == null -> {
                    isSuccess = false
                    it
                }

                // Открыт нужный нам экран
                screenKey == it.screenParams.asKey() -> {
                    isSuccess = true
                    null
                }

                // Открыт другой экран, закрывать нечего.
                else -> {
                    isSuccess = false
                    it
                }
            }
        }
        return isSuccess ?: error("unreachable")
    }
}
