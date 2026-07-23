package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
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
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder
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
    var parentNavigator: ScreenNavigatorImpl<Ctx>?,
    var screenPath: ScreenPath,
    var initialPath: ScreenPathWithIntent?,
    val node: LinkedTreeNode<ScreenInfo<Ctx>>,
    val serializer: KSerializer<IntentScreenParams<*>>,
    private val lifecycle: Lifecycle,
) : ScreenNavigator {
    /**
     * Обратная ссылка на holder, если навигатор построен на управляемом контексте.
     */
    internal var holder: TransferableScreenHolder<Ctx>? = null

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

    val screenParams: IntentScreenParams<*>
        get() = (screenPath.last() as ScreenPath.PathElement.Params).screenParams

    /**
     * Экран в контексте которого существует данный навигатор.
     */
    lateinit var screen: GenericScreen<Ctx>

    init {
        // Регистрируем этот навигатор в родительском через явный механизм
        // (не lifecycle-bound, т.к. lifecycle теперь — это lifecycle holder'а,
        //  который переживает перенос и не уходит в DESTROYED при удалении mount'а).
        parentNavigator?.registerScreenNavigatorDirect(this)

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
     * Рекурсивно обновляет [screenPath] и [parentNavigator] для этого навигатора и всего его поддерева.
     */
    fun rebase(newParent: ScreenNavigatorImpl<Ctx>?, newScreenPath: ScreenPath) {
        parentNavigator = newParent
        screenPath = newScreenPath
        childScreenNavigators.forEach { (params, child) ->
            child.rebase(this, screenPath + params)
        }
    }

    /**
     * Убирает этот навигатор из родительского [childScreenNavigators] без вызова destroy.
     */
    fun detachFromParent() {
        parentNavigator?.unregisterScreenNavigatorDirect(this)
    }

    /**
     * Регистрирует навигатор в родительском [childScreenNavigators] без привязки к lifecycle.
     */
    internal fun registerScreenNavigatorDirect(navigator: ScreenNavigatorImpl<Ctx>) {
        val oldScreenNavigator = childScreenNavigators.put(navigator.screenParams, navigator)
        check(oldScreenNavigator == null) {
            "Screen navigator for ${navigator.screenPath} already registered"
        }
    }

    /**
     * Убирает навигатор из [childScreenNavigators] без вызова destroy.
     * Идемпотентно — если навигатора нет в карте, ничего не делает.
     */
    internal fun unregisterScreenNavigatorDirect(navigator: ScreenNavigatorImpl<Ctx>) {
        childScreenNavigators.remove(navigator.screenParams)
    }

    /**
     * Возвращает стартовые параметры для [navigationHost] если таковые есть.
     *
     * Стартовые параметры != параметры по умолчанию. Стартовые параметры используются для замены путей по умолчанию
     * на кастомные путы. Это позволяет не создавать стартовые экраны если, к примеру, приложение было открыто через
     * deepLink и нам нужно показать другую иерархию экранов.
     */
    fun getInitialParamsFor(navigationHost: NavigationHost): ScreenParamsWithIntent? {
        val ip = initialPath ?: return null
        val element = ip.screenPath.first()
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
            val isTarget = ip.screenPath.size == 1
            ScreenParamsWithIntent(
                screenParams = it,
                intent = if (isTarget) ip.intent else null,
                savedInstance = if (isTarget) ip.savedInstance else null,
                providerParams = if (isTarget) ip.providerParams else null,
            )
        }
    }

    /**
     * Результат закрытия экрана.
     */
    internal class CloseResult(val closed: Boolean, val holder: TransferableScreenHolder<*>?)

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

    @PublishedApi
    internal fun registerInProviderRegistry(screenKey: ScreenKey, screenFactory: ScreenFactory<Ctx, *, *, *>) {
        globalNavigator.factoryProviderRegistry.register(
            providerParams = screenParams,
            targetKey = screenKey,
            factory = screenFactory,
        )
    }

    /**
     * Последовательно открывает все экраны в цепочке [screenPath]. Первый экран в этой цепочке это экран который
     * должен быть открыт одним из [NavigationHost] этого экрана.
     */
    // TODO после закрепления поведения тестами хочется избавиться тут от пересоздания screenPath на каждый хоп
    // что бы уменьшить количество алокаций памяти, а так же снизить алгоритмическую сложность.
    fun openChain(
        screenPath: ScreenPath,
        intent: ScreenIntent?,
        savedInstance: TransferableScreenHolder<*>? = null,
        providerParams: IntentScreenParams<*>? = null,
    ) {
        NavigationLogger.t {
            "ScreenNavigator(screenParams=$screenParams).openInsideThisScreen(screenPath=$screenPath)"
        }

        val firstScreen = screenPath.first()
        val childPath = ScreenPath(screenPath.drop(1))

        // Если открываемый экран промежуточный (за ним в цепочке есть ещё экраны) и его ещё нет, то создаём его
        // через initialPath-механизм. Тогда его хосты используют default-лямбду (как при deep-link на старте),
        // а не initial-лямбду, и оставшаяся цепочка развернётся сама. Перенос инстанса (savedInstance) при этом
        // доедет до целевого экрана через initialPath.
        if (childPath.isNotEmpty() && findChildNavigator(firstScreen) == null) {
            initialPath = ScreenPathWithIntent(screenPath, intent, savedInstance, providerParams = providerParams)
            openInsideThisScreen(screen = firstScreen, intent = null, savedInstance = null)
            return
        }

        // Открываем первый требуемый экран внутри текущего.
        openInsideThisScreen(
            screen = firstScreen,
            intent = intent?.takeIf { screenPath.size == 1 },
            savedInstance = savedInstance?.takeIf { screenPath.size == 1 },
            providerParams = providerParams?.takeIf { screenPath.size == 1 },
        )

        // Если требуется открыть более одного экрана за раз, то передаем управление дальше, навигатору экрана
        // который только что открыли шагом выше.
        if (childPath.isNotEmpty()) {
            val childNavigator = resolveChildNavigatorAfterOpen(firstScreen)
            childNavigator!!.openChain(
                screenPath = childPath,
                intent = intent,
                savedInstance = savedInstance,
                providerParams = providerParams,
            )
        }
    }

    /**
     * Пытается найти последний экран в цепочке [screenPath] и закрыть его. Первый экран в этой цепочке это экран
     * который должен находится в одном из [NavigationHost] этого экрана.
     * @return был ли фактически закрыт экран.
     */
    fun closeChain(screenPath: ScreenPath, keepInstance: Boolean = false): CloseResult {
        NavigationLogger.t {
            "ScreenNavigator(screenParams=$screenParams).closeInsideThisScreen(screenPath=$screenPath)"
        }
        return if (screenPath.size == 1) {
            // Если в цепочке один экран, то пробуем закрыть его.
            closeInsideThisScreen((screenPath.first() as ScreenPath.PathElement.Params).screenParams, keepInstance)
        } else {
            val childNavigator = findChildNavigator(childElement = screenPath.first())
            childNavigator?.closeChain(ScreenPath(screenPath.drop(1)), keepInstance)
                ?: CloseResult(false, null)
        }
    }

    /**
     * Ищет [NavigationHost] который может открыть данный [screen] и открывает его опционально передавая туда [intent].
     */
    private fun openInsideThisScreen(
        screen: ScreenPath.PathElement,
        intent: ScreenIntent?,
        savedInstance: TransferableScreenHolder<*>? = null,
        providerParams: IntentScreenParams<*>? = null,
    ) {
        val screenKey = screen.asScreenKey()
        val childNode = node.children.find { it.value.screenKey == screenKey }
            ?: error("Child node with screenKey=$screenKey not found")
        val hostNavigator = getChildHostNavigator(screenKey)
        when (screen) {
            is ScreenPath.PathElement.Key -> hostNavigator.open(screen.screenKey) { childNode.value.defaultParams!! }

            is ScreenPath.PathElement.Params -> hostNavigator.open(
                params = screen.screenParams,
                intent = intent,
                savedInstance = savedInstance,
                providerParams = providerParams,
            )
        }
    }

    /**
     * Ищет [NavigationHost] который может закрыть данный [screen] и пробует закрыть его.
     *
     * @return результат закрытия. Если [keepInstance] — пытается сохранить инстанс экрана.
     */
    private fun closeInsideThisScreen(screenParams: IntentScreenParams<*>, keepInstance: Boolean = false): CloseResult {
        val screenKey = screenParams.asKey()
        val hostNavigator = getChildHostNavigator(screenKey)
        val childNavigator = childScreenNavigators[screenParams]
        childNavigator?.holder?.navigator?.detachFromParent()
        val holder = if (keepInstance) {
            childNavigator?.holder?.also { h ->
                h.unbind()
            }
        } else {
            null
        }
        val closed = hostNavigator.close(screenParams)
        return CloseResult(closed, holder)
    }

    /**
     * Возвращает фабрику для создания дочернего экрана, не проверяя реестр провайдеров.
     */
    @Suppress("UNCHECKED_CAST")
    fun getChildScreenFactory(
        screenKey: ScreenKey,
    ): ScreenFactory<Ctx, IntentScreenParams<ScreenIntent>, ScreenIntent, *> {
        val factory = tryGetChildScreenFactory(screenKey)
        check(factory != null) { "Factory for screen $screenKey not found" }
        return factory as ScreenFactory<Ctx, IntentScreenParams<ScreenIntent>, ScreenIntent, *>
    }

    /**
     * Пытается найти фабрику для создания дочернего экрана. Возвращает null, если фабрика не найдена.
     */
    fun tryGetChildScreenFactory(screenKey: ScreenKey): ScreenFactory<Ctx, *, *, *>? = customFactories[screenKey]
        ?: node.children.find { it.value.screenKey == screenKey }?.value?.factory

    /**
     * Запрашивает задержку splash экрана, сначала для текущего экрана и только потом для всех дочерних навигаторов.
     */
    suspend fun delaySplashScreen() {
        screen.delaySplashScreenInternal()
        childScreenNavigators.values.forEach { it.delaySplashScreen() }
    }

    fun createChildNavigator(childScreenParams: IntentScreenParams<*>, childContext: Ctx): ScreenNavigatorImpl<Ctx> {
        val screenKey = ScreenKey(childScreenParams::class)
        val childNode = node.children.find { it.value.screenKey == screenKey }
        check(childNode != null) {
            "Screen ${childScreenParams.asKey()} is not a child for screen ${childScreenParams.asKey()}"
        }

        val childInitialPath: ScreenPathWithIntent? = initialPath?.let { ip ->
            if (ip.screenPath.first().asScreenKey() == screenKey) {
                val intent = ip.intent
                val savedInstance = ip.savedInstance
                val providerParams = ip.providerParams
                val tail = ip.screenPath.drop(1)
                initialPath = null
                if (tail.isNotEmpty()) {
                    ScreenPathWithIntent(ScreenPath(tail), intent, savedInstance, providerParams = providerParams)
                } else {
                    null
                }
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
            initialPath = childInitialPath,
        )
    }

    override fun <S : IntentScreenParams<I>, I : ScreenIntent> open(
        screenParams: S,
        intent: I?,
        hints: List<IntentScreenParams<*>>,
    ): Unit = globalNavigator.open(
        startScreenPath = screenPath,
        targetScreenParams = screenParams,
        intent = intent,
        hints = hints,
    )

    override fun <S : IntentScreenParams<I>, I : ScreenIntent> openWithCustomFactory(
        screenParams: S,
        intent: I?,
        hints: List<IntentScreenParams<*>>,
    ): Unit = globalNavigator.open(
        startScreenPath = screenPath,
        targetScreenParams = screenParams,
        intent = intent,
        hints = hints,
        providerParams = this.screenParams,
    )

    override fun close(screenParams: IntentScreenParams<*>): Unit =
        globalNavigator.close(startScreenPath = screenPath, targetScreenParams = screenParams)

    override fun close(): Unit = globalNavigator.close(
        startScreenPath = screenPath,
        targetScreenParams = (screenPath.last() as ScreenPath.PathElement.Params).screenParams,
    )

    override fun <S : IntentScreenParams<I>, I : ScreenIntent> transfer(
        screenParams: S,
        hints: List<IntentScreenParams<*>>,
    ): Unit = globalNavigator.transfer(
        startScreenPath = screenPath,
        targetScreenParams = screenParams,
        hints = hints,
    )

    private fun findChildNavigator(childElement: ScreenPath.PathElement): ScreenNavigatorImpl<Ctx>? =
        when (childElement) {
            is ScreenPath.PathElement.Key -> childScreenNavigators.asSequence()
                .firstOrNull { entry -> entry.key.asKey() == childElement.screenKey }?.value

            is ScreenPath.PathElement.Params -> childScreenNavigators[childElement.screenParams]
        }

    private fun resolveChildNavigatorAfterOpen(firstScreen: ScreenPath.PathElement): ScreenNavigatorImpl<Ctx>? {
        if (firstScreen is ScreenPath.PathElement.Params) {
            return childScreenNavigators[firstScreen.screenParams]
        }
        val keyElement = firstScreen as ScreenPath.PathElement.Key
        val activeParams = getChildHostNavigator(keyElement.screenKey).getActiveParams(keyElement.screenKey)
        return if (activeParams != null) {
            childScreenNavigators[activeParams]
        } else {
            findChildNavigator(firstScreen)
        }
    }

    private fun getChildHostNavigator(screenKey: ScreenKey): HostNavigator {
        val childNode = node.children.find { it.value.screenKey == screenKey }
            ?: error("Child node with screenKey=$screenKey not found")
        return navigationHosts[childNode.value.hostInParent]
            ?: error("Host navigator for host=${childNode.value.hostInParent} not found")
    }
}
