package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.navigate
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.ScreenParams
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
    initialConfiguration: () -> IntentScreenParams<ScreenIntent>? = { null },
    key: String = "slot_navigation",
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildSlot<IntentScreenParams<ScreenIntent>, Screen>> {
    val source = SlotNavigation<IntentScreenParams<ScreenIntent>>()

    val hostNavigator = SlotHostNavigator(source)
    navigator.registerHostNavigator(navigationHost, hostNavigator)

    val slot = childSlot(
        source = source,
        serializer = if (allowStateSave) navigator.serializer else null,
        key = key,
        initialConfiguration = { navigator.getInitialParamsFor(navigationHost) ?: initialConfiguration() },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )

    return slot
}

private class SlotHostNavigator(
    private val slotNavigation: SlotNavigation<IntentScreenParams<ScreenIntent>>,
) : HostNavigator {
    override fun open(params: IntentScreenParams<ScreenIntent>) {
        // Просто открываем переданный экран, логика слот навигации закроет предыдущий экран если он другой
        // или не будет делать ничего если уже открыт искомый экран.
        slotNavigation.navigate { params }
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<ScreenIntent>) {
        // Проверяем, если текущий экран имеет такой же ключ, то оставляем его, иначе заменяем на defaultParams
        slotNavigation.navigate { currentOpenedScreen ->
            if (currentOpenedScreen != null && currentOpenedScreen.asKey() == screenKey) {
                currentOpenedScreen
            } else {
                defaultParams()
            }
        }
    }

    override fun close(params: IntentScreenParams<ScreenIntent>): Boolean {
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
                screenKey == it.asKey() -> {
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
