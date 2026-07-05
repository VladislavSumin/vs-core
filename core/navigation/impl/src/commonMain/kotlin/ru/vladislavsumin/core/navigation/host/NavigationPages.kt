package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import kotlinx.serialization.Serializable
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.navigator.HostNavigator
import ru.vladislavsumin.core.navigation.screen.GenericScreen
import ru.vladislavsumin.core.navigation.screen.ScreenKey
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

/**
 * Навигация типа "страницы", означает, что в ней одновременно может быть несколько экранов, но только один из них
 * активен, при этом в отличие от стека активен может быть любой экран, а не только последний
 *
 * @param navigationHost навигационный хост для возможности понять, какие экраны будут открываться в этой навигации.
 * @param initialPages начальный набор страниц.
 * @param defaultPages набор страниц по умолчанию, используется, когда в навигации задан инициализирующий экран
 * (например через deep-link / initialPath). В отличие от [childNavigationStack], где целевой экран всегда добавляется
 * поверх стека, здесь целевой экран передаётся в лямбду параметром, чтобы его можно было разместить в любом удобном
 * месте страниц и указать выбранную страницу. Intent целевого экрана доставляется автоматически той странице, чьи
 * параметры совпадают с переданным экраном, поэтому доступ к intent в лямбде не требуется.
 * @param key уникальный в пределах экрана ключ для навигации.
 * @param pageStatus позволяет настраивать жизненный цикл страниц, см. оригинальное api Аркадия.
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 * @param allowStateSave разрешает сохранять состояние экранов открытых в данном навигаторе.
 */
public fun <Ctx : GenericComponentContext<Ctx>> GenericScreen<Ctx>.childNavigationPages(
    navigationHost: NavigationHost,
    initialPages: () -> Pages<IntentScreenParams<*>>,
    defaultPages: (params: IntentScreenParams<*>) -> Pages<IntentScreenParams<*>> = ::getDefaultInitialPages,
    key: String = "pages_navigation",
    pageStatus: (index: Int, Pages<*>) -> Status = ::getDefaultPageStatus,
    extraLifecycle: Lifecycle? = null,
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildPages<ConfigurationHolder, GenericScreen<Ctx>>> {
    val source = PagesNavigation<ConfigurationHolder>()

    val hostNavigator = PagesHostNavigator(source)
    internalNavigator.registerHostNavigator(navigationHost, hostNavigator)

    val context = if (extraLifecycle != null) internalContext.childContext(key, extraLifecycle) else internalContext

    val pages = context.childPages(
        source = source,
        savePages = { state ->
            if (allowStateSave) {
                SerializableContainer(
                    value = SerializablePages(items = state.items.map { it.screenParams }, state.selectedIndex),
                    strategy = SerializablePages.serializer(internalNavigator.serializer),
                )
            } else {
                null
            }
        },
        restorePages = { container ->
            val pages = container.consumeRequired(strategy = SerializablePages.serializer(internalNavigator.serializer))
            Pages(pages.items.map { ConfigurationHolder(it) }, pages.selectedIndex)
        },
        key = key,
        initialPages = {
            val initial = internalNavigator.getInitialParamsFor(navigationHost)
            if (initial != null) {
                val pages = defaultPages(initial.screenParams)
                val holders = pages.items.map { ConfigurationHolder(it) }
                holders.firstOrNull { it.screenParams == initial.screenParams }?.sendIntent(initial.intent)
                Pages(holders, pages.selectedIndex)
            } else {
                val initial = initialPages()
                Pages(initial.items.map { ConfigurationHolder(it) }, initial.selectedIndex)
            }
        },
        pageStatus = pageStatus,
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )
    return pages
}

@Suppress("EmptyFunctionBlock")
private class PagesHostNavigator(private val pagesNavigation: PagesNavigation<ConfigurationHolder>) : HostNavigator {
    override fun open(
        params: IntentScreenParams<*>,
        intent: ScreenIntent?,
        savedInstance: TransferableScreenHolder<*>?,
    ) {
        pagesNavigation.navigate(
            transformer = { pages ->
                val indexOfScreen = pages.items.indexOfFirst { it.screenParams == params }
                if (indexOfScreen >= 0) {
                    pages.items[indexOfScreen].sendIntent(intent)
                    pages.copy(selectedIndex = indexOfScreen)
                } else {
                    val newItem = ConfigurationHolder(params, savedInstance = savedInstance)
                    newItem.sendIntent(intent)
                    val newItems = pages.items + newItem
                    Pages(newItems, newItems.size - 1)
                }
            },
            onComplete = { _, _ -> },
        )
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<*>) {
        pagesNavigation.navigate(
            transformer = { pages ->
                val indexOfScreen = pages.items.map { it.screenParams.asKey() }.indexOf(screenKey)
                if (indexOfScreen >= 0) {
                    pages.copy(selectedIndex = indexOfScreen)
                } else {
                    val indexOfDefaultScreen = pages.items.indexOfFirst { it.screenParams == defaultParams() }
                    if (indexOfDefaultScreen >= 0) {
                        pages.copy(selectedIndex = indexOfDefaultScreen)
                    } else {
                        val newItem = ConfigurationHolder(defaultParams())
                        val newItems = pages.items + newItem
                        Pages(newItems, newItems.size - 1)
                    }
                }
            },
            onComplete = { _, _ -> },
        )
    }

    override fun close(params: IntentScreenParams<*>): Boolean {
        var isSuccess: Boolean? = null
        pagesNavigation.navigate(
            transformer = { pages ->
                val indexOfScreen = pages.items.indexOfFirst { it.screenParams == params }
                if (indexOfScreen >= 0) {
                    isSuccess = true
                    val newItems = pages.items.toMutableList()
                    newItems.removeAt(indexOfScreen)
                    val newIndex = if (indexOfScreen == pages.selectedIndex) {
                        // Если мы закрываем текущую вкладку, то пробуем сначала индекс права, а если его нет, то слева.
                        if (indexOfScreen == newItems.size) {
                            pages.selectedIndex - 1
                        } else {
                            pages.selectedIndex
                        }
                    } else {
                        // Если мы удаляем не текущую открытую вкладку, то сохраняем новый индекс открытой вкладки
                        if (indexOfScreen < pages.selectedIndex) {
                            pages.selectedIndex - 1
                        } else {
                            pages.selectedIndex
                        }
                    }
                    pages.copy(items = newItems, selectedIndex = newIndex)
                } else {
                    isSuccess = false
                    pages
                }
            },
            onComplete = { _, _ -> },
        )
        return isSuccess ?: error("Unreachable")
    }

    override fun close(screenKey: ScreenKey): Boolean {
        var isSuccess: Boolean? = null
        pagesNavigation.navigate(
            transformer = { pages ->
                val indexOfScreen = pages.items.indexOfFirst { it.screenParams.asKey() == screenKey }
                if (indexOfScreen >= 0) {
                    isSuccess = true
                    val newItems = pages.items.toMutableList()
                    newItems.removeAt(indexOfScreen)
                    val newIndex = if (indexOfScreen == pages.selectedIndex) {
                        // Если мы закрываем текущую вкладку, то пробуем сначала индекс права, а если его нет, то слева.
                        if (indexOfScreen == newItems.size) {
                            pages.selectedIndex - 1
                        } else {
                            pages.selectedIndex
                        }
                    } else {
                        // Если мы удаляем не текущую открытую вкладку, то сохраняем новый индекс открытой вкладки
                        if (indexOfScreen < pages.selectedIndex) {
                            pages.selectedIndex - 1
                        } else {
                            pages.selectedIndex
                        }
                    }
                    pages.copy(items = newItems, selectedIndex = newIndex)
                } else {
                    isSuccess = false
                    pages
                }
            },
            onComplete = { _, _ -> },
        )
        return isSuccess ?: error("Unreachable")
    }
}

/**
 * Копия оригинального параметра по умолчанию из api Аркадия.
 */
@PublishedApi
internal fun getDefaultPageStatus(index: Int, pages: Pages<*>): Status = when (index) {
    pages.selectedIndex -> Status.RESUMED
    in (pages.selectedIndex - 1)..(pages.selectedIndex + 1) -> Status.CREATED
    else -> Status.DESTROYED
}

/**
 * Значение по умолчанию для параметра `defaultPages` в [childNavigationPages]. Размещает целевой экран единственной
 * выбранной страницей, повторяя поведение, которое было до появления параметра `defaultPages`.
 */
@PublishedApi
internal fun getDefaultInitialPages(params: IntentScreenParams<*>): Pages<IntentScreenParams<*>> =
    Pages(listOf(params), 0)

@Serializable
private class SerializablePages<T : IntentScreenParams<*>>(val items: List<T>, val selectedIndex: Int)
