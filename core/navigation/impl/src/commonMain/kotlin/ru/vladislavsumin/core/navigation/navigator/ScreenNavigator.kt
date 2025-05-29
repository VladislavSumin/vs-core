package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.serialization.KSerializer
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.NavigationLogger
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

/**
 * Навигатор уровня экрана.
 *
 * @param globalNavigator ссылка на global навигатор.
 * @param screenPath путь до экрана соответствующего данному навигатору.
 * @param node нода соответствующая этому экрану в графе навигации.
 * @param lifecycle жизненный цикл компонента к которому привязан этот навигатор.
 */
public class ScreenNavigator internal constructor(
    internal val globalNavigator: GlobalNavigator,
    internal val parentNavigator: ScreenNavigator?,
    internal val screenPath: ScreenPath,
    internal val node: LinkedTreeNode<ScreenInfo>,
    internal val serializer: KSerializer<IntentScreenParams<*>>,
    private val lifecycle: Lifecycle,
    internal val initialPath: ScreenPath?,
) {
    /**
     * Список зарегистрированных на этом экране [HostNavigator].
     */
    private val navigationHosts = mutableMapOf<NavigationHost, HostNavigator>()

    /**
     * Зарегистрированные кастомные фабрики экранов открываемых из хостов этого экрана.
     */
    private val customFactories = mutableMapOf<ScreenKey, ScreenFactory<*, *, *>>()

    /**
     * Текущие активные навигаторы среди дочерних экранов.
     */
    private val childScreenNavigators = mutableMapOf<IntentScreenParams<*>, ScreenNavigator>()

    internal val screenParams = (screenPath.last() as ScreenPath.PathElement.Params).screenParams

    /**
     * Экран в контексте которого существует данный навигатор.
     */
    internal lateinit var screen: Screen

    init {
        // Регистрируем этот навигатор в родительском.
        parentNavigator?.registerScreenNavigator(this, lifecycle)

        lifecycle.doOnCreate {
            // Проверяем что экран зарегистрировал кастомные фабрики для всех дочерних экранов которые требуют таковых.
            val screenWithoutFactory = node.children
                .filter { it.value.factory == null }
                .map { it.value.screenKey }
                .toSet()
            val registeredScreenFactory = customFactories.keys
            check(screenWithoutFactory == registeredScreenFactory) {
                "Actual factory registration wrong. Expected $screenWithoutFactory, actual $registeredScreenFactory"
            }

            // Проверяем что экран действительно зарегистрировал все типы навигации которые может открывать.
            val expectedHosts = node.value.navigationHosts
            val actualHosts = navigationHosts.keys
            check(expectedHosts == actualHosts) {
                "Actual host registration doesn't match expected. Actual:$actualHosts, expected:$expectedHosts"
            }
        }
    }

    /**
     * Возвращает стартовые параметры для [navigationHost] если таковые есть.
     */
    internal fun getInitialParamsFor(navigationHost: NavigationHost): IntentScreenParams<*>? {
        val element = initialPath?.first() ?: return null
        val screenKey = element.asErasedKey()
        val childNode = node.children.find { it.value.screenKey == screenKey }?.value
            ?: error("Child node with screenKey=$screenKey not found")
        return if (childNode.hostInParent == navigationHost) {
            when (element) {
                is ScreenPath.PathElement.Key -> childNode.defaultParams ?: error("No default params")
                is ScreenPath.PathElement.Params -> element.screenParams
            }
        } else {
            null
        }
    }

    /**
     * Регистрирует [screenNavigator] с учетом жизненного цикла [ComponentContext].
     */
    internal fun registerScreenNavigator(screenNavigator: ScreenNavigator, lifecycle: Lifecycle) {
        val oldScreenNavigator = childScreenNavigators.put(screenNavigator.screenParams, screenNavigator)
        check(oldScreenNavigator == null) {
            "Screen navigator for ${screenNavigator.screenPath} already registered"
        }
        lifecycle.doOnDestroy {
            val navigator = childScreenNavigators.remove(screenNavigator.screenParams)
            check(navigator != null) { "Screen navigator for ${screenNavigator.screenPath} not found" }
        }
    }

    /**
     * Регистрирует [HostNavigator] для [NavigationHost] навигации. Учитывает lifecycle [ScreenContext].
     */
    internal fun registerHostNavigator(navigationHost: NavigationHost, hostNavigator: HostNavigator) {
        val oldHost = navigationHosts.put(navigationHost, hostNavigator)
        check(oldHost == null) { "Navigation host $navigationHost already registered" }
    }

    /**
     * Регистрирует [ScreenFactory] для [screenKey] навигации. Учитывает lifecycle [ScreenContext].
     */
    @PublishedApi
    internal fun <S : IntentScreenParams<I>, I : ScreenIntent> registerCustomFactory(
        screenKey: ScreenKey,
        screenFactory: ScreenFactory<S, I, *>,
    ) {
        // Важно, чтобы фабрики регистрировались до инициализации навигационных хостов. Иначе при восстановлении
        // состояния навигационные хосты будут восстановлены до регистрации фабрик, а следовательно не смогут создать
        // экран.
        check(navigationHosts.isEmpty()) {
            "You must register screen factory before any navigation host, now registered hosts ${navigationHosts.keys}"
        }

        val oldCustomFactory = customFactories.put(screenKey, screenFactory)
        check(oldCustomFactory == null) { "Custom factory for $screenKey already registered" }
    }

    internal fun openInsideThisScreen(screenPath: ScreenPath, intent: ScreenIntent?) {
        NavigationLogger.t {
            "ScreenNavigator(screenParams=$screenParams).openInsideThisScreen(screenPath=$screenPath)"
        }
        openInsideThisScreen(screenPath.first(), intent?.takeIf { screenPath.size == 1 })
        val childPath = ScreenPath(screenPath.drop(1))
        if (childPath.isNotEmpty()) {
            val childElement = screenPath.first()
            val childNavigator = when (childElement) {
                is ScreenPath.PathElement.Key -> childScreenNavigators.asSequence()
                    .first { entry -> entry.key.asKey() == childElement.screenKey }.value

                is ScreenPath.PathElement.Params -> childScreenNavigators[childElement.screenParams]
            }
            childNavigator!!.openInsideThisScreen(childPath, intent)
        }
    }

    /**
     * Внутренняя функция предназначенная для использования глобальным навигатором.
     * После нахождения пути к экрану открываемому через вызов [open], навигатор последовательно вызывает эту
     * функцию на **каждом** экране в пути, тем самым переключая состояние на требуемое.
     */
    private fun openInsideThisScreen(screen: ScreenPath.PathElement, intent: ScreenIntent?) {
        val screenKey = screen.asErasedKey()
        val childNode = node.children.find { it.value.screenKey == screenKey }
            ?: error("Child node with screenKey=$screenKey not found")
        val hostNavigator = navigationHosts[childNode.value.hostInParent]
            ?: error("Host navigator for host=${childNode.value.hostInParent} not found")
        when (screen) {
            is ScreenPath.PathElement.Key -> hostNavigator.open(screen.screenKey) { childNode.value.defaultParams!! }
            is ScreenPath.PathElement.Params -> hostNavigator.open(screen.screenParams, intent)
        }
    }

    internal fun closeInsideThisScreen(screenPath: ScreenPath) {
        NavigationLogger.t {
            "ScreenNavigator(screenParams=$screenParams).closeInsideThisScreen(screenPath=$screenPath)"
        }
        if (screenPath.size == 1) {
            closeInsideThisScreen((screenPath.first() as ScreenPath.PathElement.Params).screenParams)
        } else {
            val childElement = screenPath.firstOrNull() ?: return
            val childNavigator = when (childElement) {
                is ScreenPath.PathElement.Key -> childScreenNavigators.asSequence()
                    .first { entry -> entry.key.asKey() == childElement.screenKey }.value

                is ScreenPath.PathElement.Params -> childScreenNavigators[childElement.screenParams]
            }
            childNavigator?.closeInsideThisScreen(ScreenPath(screenPath.drop(1)))
        }
    }

    private fun closeInsideThisScreen(screenParams: IntentScreenParams<*>) {
        // TODO убрать дублирование кода.
        val screenKey = ScreenKey(screenParams::class)
        val childNode = node.children.find { it.value.screenKey == screenKey }
            ?: error("Child node with screenKey=$screenKey not found")
        val hostNavigator = navigationHosts[childNode.value.hostInParent]
            ?: error("Host navigator for host=${childNode.value.hostInParent} not found")
        hostNavigator.close(screenParams)
    }

    /**
     * Возвращает фабрику для создания дочернего экрана.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun getChildScreenFactory(
        screenKey: ScreenKey,
    ): ScreenFactory<IntentScreenParams<ScreenIntent>, ScreenIntent, *> {
        // Ищем среди локальных фабрик, потом, если не нашли, смотрим в глобальных фабриках.
        val factory = customFactories[screenKey]
            ?: node.children.find { it.value.screenKey == screenKey }!!.value.factory
        return factory as ScreenFactory<IntentScreenParams<ScreenIntent>, ScreenIntent, *>
    }

    /**
     * Запрашивает задержку splash экрана, сначала для текущего экрана и только потом для всех дочерних навигаторов.
     */
    internal suspend fun delaySplashScreen() {
        screen.delaySplashScreenInternal()
        childScreenNavigators.values.forEach { it.delaySplashScreen() }
    }

    /**
     * Открывает экран соответствующий переданным [screenParams], при этом, при поиске места открытия экрана учитывается
     * текущее место. (подробнее про приоритет выбора места написано в документации).
     */
    public fun <S : IntentScreenParams<I>, I : ScreenIntent> open(screenParams: S, intent: I? = null): Unit =
        globalNavigator.open(screenPath, screenParams, intent)

    public fun close(screenParams: IntentScreenParams<*>): Unit = globalNavigator.close(screenPath, screenParams)
    public fun close(): Unit = globalNavigator.close(
        screenPath,
        (screenPath.last() as ScreenPath.PathElement.Params).screenParams,
    )
}
