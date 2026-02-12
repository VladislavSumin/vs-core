package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.serialization.KSerializer
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.NavigationLogger
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenFactory
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.ScreenPathWithIntent
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
@PublishedApi
internal class ScreenNavigatorImpl<Ctx : GenericComponentContext<Ctx>>(
    val globalNavigator: GlobalNavigator<Ctx>,
    val parentNavigator: ScreenNavigatorImpl<Ctx>?,
    val screenPath: ScreenPath,
    val initialPath: ScreenPathWithIntent?,
    val node: LinkedTreeNode<ScreenInfo<Ctx>>,
    val serializer: KSerializer<IntentScreenParams<*>>,
    private val lifecycle: Lifecycle,
) : ScreenNavigator {
    /**
     * Список зарегистрированных на этом экране [HostNavigator].
     */
    private val navigationHosts = mutableMapOf<NavigationHost, HostNavigator>()

    /**
     * Зарегистрированные кастомные фабрики экранов открываемых из хостов этого экрана.
     */
    private val customFactories = mutableMapOf<ScreenKey, ScreenFactory<Ctx, *, *, *>>()

    /**
     * Текущие активные навигаторы среди дочерних экранов.
     */
    private val childScreenNavigators = mutableMapOf<IntentScreenParams<*>, ScreenNavigatorImpl<Ctx>>()

    val screenParams = (screenPath.last() as ScreenPath.PathElement.Params).screenParams

    /**
     * Экран в контексте которого существует данный навигатор.
     */
    lateinit var screen: GenericScreen<Ctx>

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
     *
     * Стартовые параметры != параметры по умолчанию. Стартовые параметры используются для замены путей по умолчанию
     * на кастомные путы. Это позволяет не создавать стартовые экраны если, к примеру, приложение было открыто через
     * deepLink и нам нужно показать другую иерархию экранов.
     */
    fun getInitialParamsFor(navigationHost: NavigationHost): ScreenParamsWithIntent? {
        val element = initialPath?.screenPath?.first() ?: return null
        val screenKey = element.asScreenKey()
        val childNode = node.children.find { it.value.screenKey == screenKey }?.value
            ?: error("Child node with screenKey=$screenKey not found")
        val screenParams = if (childNode.hostInParent == navigationHost) {
            when (element) {
                is ScreenPath.PathElement.Key -> childNode.defaultParams ?: error("No default params")
                is ScreenPath.PathElement.Params -> element.screenParams
            }
        } else {
            null
        }
        return screenParams?.let {
            val intent = if (initialPath.screenPath.size == 1) {
                initialPath.intent
            } else {
                null
            }
            ScreenParamsWithIntent(it, intent)
        }
    }

    /**
     * Регистрирует [screenNavigator] с учетом жизненного цикла [ComponentContext].
     */
    fun registerScreenNavigator(screenNavigator: ScreenNavigatorImpl<Ctx>, lifecycle: Lifecycle) {
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
    fun registerHostNavigator(navigationHost: NavigationHost, hostNavigator: HostNavigator) {
        val oldHost = navigationHosts.put(navigationHost, hostNavigator)
        check(oldHost == null) { "Navigation host $navigationHost already registered" }
    }

    /**
     * Регистрирует [ScreenFactory] для [screenKey] навигации. Учитывает lifecycle [ScreenContext].
     */
    fun <S : IntentScreenParams<I>, I : ScreenIntent> registerCustomFactory(
        screenKey: ScreenKey,
        screenFactory: ScreenFactory<Ctx, S, I, *>,
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

    /**
     * Последовательно открывает все экраны в цепочке [screenPath]. Первый экран в этой цепочке это экран который
     * должен быть открыт одним из [NavigationHost] этого экрана.
     */
    // TODO после закрепления поведения тестами хочется избавиться тут от пересоздания screenPath на каждый хоп
    // что бы уменьшить количество алокаций памяти, а так же снизить алгоритмическую сложность.
    fun openChain(screenPath: ScreenPath, intent: ScreenIntent?) {
        NavigationLogger.t {
            "ScreenNavigator(screenParams=$screenParams).openInsideThisScreen(screenPath=$screenPath)"
        }

        // Открываем первый требуемый экран внутри текущего.
        openInsideThisScreen(screenPath.first(), intent?.takeIf { screenPath.size == 1 })

        // Если требуется открыть более одного экрана за раз, то передаем управление дальше, навигатору экрана
        // который только что открыли шагом выше.
        val childPath = ScreenPath(screenPath.drop(1))
        if (childPath.isNotEmpty()) {
            val childNavigator = findChildNavigator(childElement = screenPath.first())
            childNavigator!!.openChain(screenPath = childPath, intent = intent)
        }
    }

    /**
     * Пытается найти последний экран в цепочке [screenPath] и закрыть его. Первый экран в этой цепочке это экран
     * который должен находится в одном из [NavigationHost] этого экрана.
     * @return был ли фактически закрыт экран.
     */
    fun closeChain(screenPath: ScreenPath): Boolean {
        NavigationLogger.t {
            "ScreenNavigator(screenParams=$screenParams).closeInsideThisScreen(screenPath=$screenPath)"
        }
        return if (screenPath.size == 1) {
            // Если в цепочке один экран, то пробуем закрыть его.
            closeInsideThisScreen((screenPath.first() as ScreenPath.PathElement.Params).screenParams)
        } else {
            val childNavigator = findChildNavigator(childElement = screenPath.first())
            childNavigator?.closeChain(ScreenPath(screenPath.drop(1))) ?: false
        }
    }

    /**
     * Ищет [NavigationHost] который может открыть данный [screen] и открывает его опционально передавая туда [intent].
     */
    private fun openInsideThisScreen(screen: ScreenPath.PathElement, intent: ScreenIntent?) {
        val screenKey = screen.asScreenKey()
        val childNode = node.children.find { it.value.screenKey == screenKey }
            ?: error("Child node with screenKey=$screenKey not found")
        val hostNavigator = getChildHostNavigator(screenKey)
        when (screen) {
            is ScreenPath.PathElement.Key -> hostNavigator.open(screen.screenKey) { childNode.value.defaultParams!! }
            is ScreenPath.PathElement.Params -> hostNavigator.open(screen.screenParams, intent)
        }
    }

    /**
     * Ищет [NavigationHost] который может закрыть данный [screen] и пробует закрыть его.
     *
     * @return был ли фактически закрыт экран.
     */
    private fun closeInsideThisScreen(screenParams: IntentScreenParams<*>): Boolean {
        val hostNavigator = getChildHostNavigator(screenParams.asKey())
        return hostNavigator.close(screenParams)
    }

    /**
     * Возвращает фабрику для создания дочернего экрана.
     */
    @Suppress("UNCHECKED_CAST")
    fun getChildScreenFactory(
        screenKey: ScreenKey,
    ): ScreenFactory<Ctx, IntentScreenParams<ScreenIntent>, ScreenIntent, *> {
        // Ищем среди локальных фабрик, потом, если не нашли, смотрим в глобальных фабриках.
        val factory = customFactories[screenKey]
            ?: node.children.find { it.value.screenKey == screenKey }!!.value.factory
        return factory as ScreenFactory<Ctx, IntentScreenParams<ScreenIntent>, ScreenIntent, *>
    }

    /**
     * Запрашивает задержку splash экрана, сначала для текущего экрана и только потом для всех дочерних навигаторов.
     */
    suspend fun delaySplashScreen() {
        screen.delaySplashScreenInternal()
        childScreenNavigators.values.forEach { it.delaySplashScreen() }
    }

    fun createChildNavigator(
        childScreenParams: IntentScreenParams<*>,
        childContext: Ctx,
    ): ScreenNavigatorImpl<Ctx> {
        val screenKey = ScreenKey(childScreenParams::class)
        val childNode = node.children.find { it.value.screenKey == screenKey }
        check(childNode != null) {
            "Screen ${childScreenParams.asKey()} is not a child for screen ${childScreenParams.asKey()}"
        }

        val newInitialPath = initialPath?.screenPath?.let { path ->
            val newPath = path.drop(1)
            if (newPath.isNotEmpty()) {
                ScreenPath(newPath)
            } else {
                null
            }
        }

        return ScreenNavigatorImpl(
            globalNavigator = globalNavigator,
            parentNavigator = this,
            screenPath = screenPath + childScreenParams,
            node = childNode,
            serializer = serializer,
            lifecycle = childContext.lifecycle,
            initialPath = newInitialPath?.let { ScreenPathWithIntent(it, initialPath.intent) },
        )
    }

    override fun <S : IntentScreenParams<I>, I : ScreenIntent> open(screenParams: S, intent: I?): Unit =
        globalNavigator.open(startScreenPath = screenPath, targetScreenParams = screenParams, intent)

    override fun close(screenParams: IntentScreenParams<*>): Unit =
        globalNavigator.close(startScreenPath = screenPath, targetScreenParams = screenParams)

    override fun close(): Unit = globalNavigator.close(
        startScreenPath = screenPath,
        targetScreenParams = (screenPath.last() as ScreenPath.PathElement.Params).screenParams,
    )

    private fun findChildNavigator(childElement: ScreenPath.PathElement): ScreenNavigatorImpl<Ctx>? {
        return when (childElement) {
            is ScreenPath.PathElement.Key -> childScreenNavigators.asSequence()
                .firstOrNull { entry -> entry.key.asKey() == childElement.screenKey }?.value

            is ScreenPath.PathElement.Params -> childScreenNavigators[childElement.screenParams]
        }
    }

    private fun getChildHostNavigator(screenKey: ScreenKey): HostNavigator {
        val childNode = node.children.find { it.value.screenKey == screenKey }
            ?: error("Child node with screenKey=$screenKey not found")
        return navigationHosts[childNode.value.hostInParent]
            ?: error("Host navigator for host=${childNode.value.hostInParent} not found")
    }
}
