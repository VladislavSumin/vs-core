package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.HostNavigator
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

/**
 * Навигация типа "стек": одновременно в ней может находиться несколько экранов, но пользователю виден только
 * последний (верхний). Открытие нового экрана кладёт его поверх стека, а закрытие снимает верхний экран.
 *
 * @param navigationHost навигационный хост для возможности понять, какие экраны будут открываться в этой навигации.
 * @param defaultStack стек экранов, который подкладывается под целевой экран при deep-link переходе (когда экран в
 * этой навигации открывают ещё до её создания, например через initialPath). Определяет, какие экраны должны лежать
 * под целевым, чтобы после перехода по ссылке кнопка "назад" работала корректно. Если целевой экран уже есть в
 * [defaultStack], стек обрезается до него; иначе целевой экран кладётся поверх [defaultStack].
 * @param initialStack стек, который открывается при обычном (не deep-link) старте навигации. По умолчанию совпадает
 * с [defaultStack]. Обратите внимание: стек должен содержать как минимум один элемент.
 * @param key уникальный в пределах экрана ключ для навигации.
 * @param closeParentWhenEmpty закрывает родительский экран при попытке закрыть последний оставшийся экран в этом стеке.
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 * @param allowStateSave разрешает сохранять состояние экранов открытых в данном навигаторе.
 */
public fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.childNavigationStack(
    navigationHost: NavigationHost,
    defaultStack: () -> List<IntentScreenParams<*>> = { emptyList() },
    initialStack: () -> List<IntentScreenParams<*>> = defaultStack,
    key: String = "stack_navigation",
    closeParentWhenEmpty: Boolean = false,
    extraLifecycle: Lifecycle? = null,
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildStack<ConfigurationHolder, GenericScreen<Ctx>>> {
    val source = StackNavigation<ConfigurationHolder>()

    val hostNavigator = StackHostNavigator(source) {
        if (closeParentWhenEmpty) {
            internalNavigator.close()
            true
        } else {
            false
        }
    }
    internalNavigator.registerHostNavigator(navigationHost, hostNavigator)

    val context = if (extraLifecycle != null) internalContext.childContext(key, extraLifecycle) else internalContext

    val stack = context.childStack(
        source = source,
        saveStack = { state ->
            if (allowStateSave) {
                SerializableContainer(
                    value = state.map { StackItem(it.screenParams, it.providerParams) },
                    strategy = ListSerializer(StackItem.serializer(internalNavigator.serializer)),
                )
            } else {
                null
            }
        },
        restoreStack = { container ->
            container
                .consumeRequired(strategy = ListSerializer(StackItem.serializer(internalNavigator.serializer)))
                .map { ConfigurationHolder(it.screenParams, providerParams = it.providerParams) }
        },
        key = key,
        initialStack = {
            val initial = internalNavigator.getInitialParamsFor(navigationHost)
            val stack = if (initial != null) {
                val paramsList = defaultStack()
                val index = paramsList.indexOfFirst { it == initial.screenParams }
                val holders = if (index >= 0) {
                    paramsList.subList(0, index + 1).map { ConfigurationHolder(it) }
                } else {
                    paramsList.map { ConfigurationHolder(it) } + ConfigurationHolder(
                        initial.screenParams,
                        initial.intent,
                        savedInstance = initial.savedInstance,
                        providerParams = initial.providerParams,
                    )
                }
                holders
            } else {
                initialStack().map { ConfigurationHolder(it) }
            }

            stack
        },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )
    return stack
}

private class StackHostNavigator(
    private val stackNavigation: StackNavigation<ConfigurationHolder>,
    /**
     * Пытается закрыть родительский экран когда закрывается последний оставшийся экран стека.
     * @return `true` если родительский экран будет закрыт (в этом случае стек не нужно опустошать самостоятельно).
     */
    private val closeParentIfEmpty: () -> Boolean,
) : HostNavigator {
    private var activeParams: IntentScreenParams<*>? = null
    private var activeScreenKey: ScreenKey? = null
    override fun open(
        params: IntentScreenParams<*>,
        intent: ScreenIntent?,
        savedInstance: TransferableScreenHolder<*>?,
        providerParams: IntentScreenParams<*>?,
    ) {
        // Если такого экрана еще нет в стеке, то открываем его.
        // Если же экран уже есть в стеке, то закрываем все экраны после него.
        stackNavigation.navigate(
            transformer = { stack ->
                val indexOfScreen = stack.indexOfFirst { it.screenParams == params }
                val newStack = if (indexOfScreen >= 0) {
                    stack.subList(0, indexOfScreen + 1)
                } else {
                    stack + ConfigurationHolder(params, savedInstance = savedInstance, providerParams = providerParams)
                }
                newStack.last().sendIntent(intent)
                newStack
            },
            onComplete = { _, _ -> },
        )
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<*>) {
        // Если экрана с таким ключом еще нет в стеке, то открываем его используя defaultParams.
        // Если же экран с таким ключом уже есть в стеке, то закрываем все экраны после него.
        stackNavigation.navigate(
            transformer = { stack ->
                val indexOfScreen = stack.map { it.screenParams.asKey() }.indexOf(screenKey)
                if (indexOfScreen >= 0) {
                    activeParams = stack[indexOfScreen].screenParams
                    activeScreenKey = screenKey
                    stack.subList(0, indexOfScreen + 1)
                } else {
                    val params = defaultParams()
                    activeParams = params
                    activeScreenKey = screenKey
                    stack + ConfigurationHolder(params)
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
        // Если закрывается последний оставшийся экран стека, то при [closeParentIfEmpty] закрываем родительский экран.
        var isSuccess: Boolean? = null
        stackNavigation.navigate(
            transformer = { stack ->
                val indexOfScreen = stack.indexOfFirst { it.screenParams == params }
                when {
                    indexOfScreen > 0 -> {
                        isSuccess = true
                        stack.subList(0, indexOfScreen)
                    }

                    indexOfScreen == 0 && stack.size == 1 && closeParentIfEmpty() -> {
                        isSuccess = true
                        stack
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

                    indexOfScreen == 0 && stack.size == 1 && closeParentIfEmpty() -> {
                        isSuccess = true
                        stack
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

    override fun getActiveParams(screenKey: ScreenKey): IntentScreenParams<*>? {
        val params = activeParams
        return if (activeScreenKey == screenKey && params != null) params else null
    }
}

@Serializable
private class StackItem<T : IntentScreenParams<*>>(
    val screenParams: T,
    val providerParams: IntentScreenParams<*>? = null,
)
