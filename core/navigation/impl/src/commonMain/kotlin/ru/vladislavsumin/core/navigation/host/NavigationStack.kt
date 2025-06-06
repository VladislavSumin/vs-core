package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import kotlinx.serialization.builtins.ListSerializer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.HostNavigator
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.asKey

/**
 * Навигация типа "стек", означает, что в ней одновременно может быть несколько экранов, но только последний из них
 * виден пользователю.
 *
 * @param navigationHost навигационный хост для возможности понять, какие экраны будут открываться в этой навигации.
 * @param defaultStack стек по умолчанию, используется, когда в навигации задан инициализирующий стек, чтобы добавить
 * экраны под экран, который будет открыт на этом стеке.
 * @param initialStack начальный стек, который будет открыт в данной навигации. Обратите внимание стек должен содержать
 * как минимум один элемент.
 * @param key уникальный в пределах экрана ключ для навигации.
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 * @param allowStateSave разрешает сохранять состояние экранов открытых в данном навигаторе.
 */
public fun ScreenContext.childNavigationStack(
    navigationHost: NavigationHost,
    defaultStack: () -> List<IntentScreenParams<*>> = { emptyList() },
    initialStack: () -> List<IntentScreenParams<*>> = defaultStack,
    key: String = "stack_navigation",
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildStack<ConfigurationHolder, Screen>> {
    val source = StackNavigation<ConfigurationHolder>()

    val hostNavigator = StackHostNavigator(source)
    navigator.registerHostNavigator(navigationHost, hostNavigator)

    val stack = childStack(
        source = source,
        saveStack = { state ->
            if (allowStateSave) {
                SerializableContainer(
                    value = state.map { it.screenParams },
                    strategy = ListSerializer(navigator.serializer),
                )
            } else {
                null
            }
        },
        restoreStack = { container ->
            container.consumeRequired(strategy = ListSerializer(navigator.serializer))
                .map { ConfigurationHolder(it) }
        },
        key = key,
        initialStack = {
            val stack = navigator.getInitialParamsFor(navigationHost)?.let { params ->
                val stack = defaultStack()
                val index = stack.indexOf(params)
                if (index >= 0) {
                    stack.subList(0, index + 1)
                } else {
                    stack + params
                }
            } ?: initialStack()

            stack.map { ConfigurationHolder(it) }
        },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )
    return stack
}

private class StackHostNavigator(
    private val stackNavigation: StackNavigation<ConfigurationHolder>,
) : HostNavigator {
    override fun open(params: IntentScreenParams<*>, intent: ScreenIntent?) {
        // Если такого экрана еще нет в стеке, то открываем его.
        // Если же экран уже есть в стеке, то закрываем все экраны после него.
        stackNavigation.navigate(
            transformer = { stack ->
                val indexOfScreen = stack.indexOfFirst { it.screenParams == params }
                val newStack = if (indexOfScreen >= 0) {
                    stack.subList(0, indexOfScreen + 1)
                } else {
                    stack + ConfigurationHolder(params)
                }
                if (intent != null) {
                    newStack.last().intents.trySend(intent).getOrThrow()
                }
                newStack
            },
            onComplete = { _, _ -> },
        )
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<ScreenIntent>) {
        // Если экрана с таким ключом еще нет в стеке, то открываем его используя defaultParams.
        // Если же экран с таким ключом уже есть в стеке, то закрываем все экраны после него.
        stackNavigation.navigate(
            transformer = { stack ->
                val indexOfScreen = stack.map { it.screenParams.asKey() }.indexOf(screenKey)
                if (indexOfScreen >= 0) {
                    stack.subList(0, indexOfScreen + 1)
                } else {
                    stack + ConfigurationHolder(defaultParams())
                }
            },
            onComplete = { _, _ -> },
        )
    }

    override fun close(params: IntentScreenParams<*>): Boolean {
        // Если закрываемый экран расположен первым, то закрываем все экраны КРОМЕ этого, так как в стеке должен быть
        // хотя бы один экран.
        // Если закрываемый экран расположен вторым или далее, то закрываем этот экран и все после него.
        // Если закрываемого экрана нет в стеке, то ничего не делаем.
        var isSuccess: Boolean? = null
        stackNavigation.navigate(
            transformer = { stack ->
                val indexOfScreen = stack.indexOfFirst { it.screenParams == params }
                when {
                    indexOfScreen > 0 -> {
                        isSuccess = true
                        stack.subList(0, indexOfScreen)
                    }

                    indexOfScreen == 0 -> {
                        isSuccess = false
                        stack.subList(0, 1)
                    }

                    else -> {
                        isSuccess = false
                        stack
                    }
                }
            },
            onComplete = { _, _ -> },
        )
        return isSuccess ?: error("unreachable")
    }

    override fun close(screenKey: ScreenKey): Boolean {
        // То же самое как при закрытии по инстансу экрана, но ищем по ключу с конца стека до первого найденного экрана.
        var isSuccess: Boolean? = null
        stackNavigation.navigate(
            transformer = { stack ->
                val keysStack = stack.map { it.screenParams.asKey() }
                val indexOfScreen = keysStack.indexOfLast { it == screenKey }
                when {
                    indexOfScreen > 0 -> {
                        isSuccess = true
                        stack.subList(0, indexOfScreen)
                    }

                    indexOfScreen == 0 -> {
                        isSuccess = false
                        stack.subList(0, 1)
                    }

                    else -> {
                        isSuccess = false
                        stack
                    }
                }
            },
            onComplete = { _, _ -> },
        )
        return isSuccess ?: error("unreachable")
    }
}
