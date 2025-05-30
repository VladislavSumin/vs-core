package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.consumeRequired
import kotlinx.serialization.Serializable
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
 * Навигация типа "страницы", означает, что в ней одновременно может быть несколько экранов, но только один из них
 * активен, при этом в отличие от стека активен может быть любой экран, а не только последний
 *
 * @param navigationHost навигационный хост для возможности понять, какие экраны будут открываться в этой навигации.
 * @param initialPages начальный набор страниц.
 * @param key уникальный в пределах экрана ключ для навигации.
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 * @param allowStateSave разрешает сохранять состояние экранов открытых в данном навигаторе.
 */
public fun ScreenContext.childNavigationPages(
    navigationHost: NavigationHost,
    initialPages: () -> Pages<IntentScreenParams<*>>,
    key: String = "pages_navigation",
    handleBackButton: Boolean = false,
    allowStateSave: Boolean = true,
): Value<ChildPages<*, Screen>> {
    val source = PagesNavigation<ConfigurationHolder>()

    val hostNavigator = PagesHostNavigator(source)
    navigator.registerHostNavigator(navigationHost, hostNavigator)

    val pages = childPages(
        source = source,
        savePages = { state ->
            if (allowStateSave) {
                SerializableContainer(
                    value = SerializablePages(items = state.items.map { it.screenParams }, state.selectedIndex),
                    strategy = SerializablePages.serializer(navigator.serializer)
                )
            } else {
                null
            }
        },
        restorePages = { container ->
            val pages = container.consumeRequired(strategy = SerializablePages.serializer(navigator.serializer))
            Pages(pages.items.map { ConfigurationHolder(it) }, pages.selectedIndex)
        },
        key = key,
        initialPages = {
            val pages = navigator.getInitialParamsFor(navigationHost)
                ?.let { Pages(listOf(it), 0) }
                ?: initialPages()
            Pages(pages.items.map { ConfigurationHolder(it) }, pages.selectedIndex)
        },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )
    return pages
}

@Suppress("EmptyFunctionBlock")
private class PagesHostNavigator(
    private val pagesNavigation: PagesNavigation<ConfigurationHolder>,
) : HostNavigator {
    override fun open(params: IntentScreenParams<*>, intent: ScreenIntent?) {
        // Переключение между экранами, определёнными в initialPages
        // Если экран не найден, то активный экран не изменяется
        pagesNavigation.navigate(
            transformer = { pages ->
                val indexOfScreen = pages.items.indexOfFirst { it.screenParams == params }
                if (indexOfScreen >= 0) {
                    if (intent != null) {
                        pages.items[indexOfScreen].intents.trySend(intent).getOrThrow()
                    }
                    pages.copy(selectedIndex = indexOfScreen)
                } else {
                    pages
                }
            },
            onComplete = { _, _ -> },
        )
    }

    override fun open(screenKey: ScreenKey, defaultParams: () -> IntentScreenParams<ScreenIntent>) {
        // Если экрана с таким ключом определён в initialPages, то активируем его
        // иначе пытаемся активировать экран использую defaultParams
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
                        pages
                    }
                }
            },
            onComplete = { _, _ -> },
        )
    }

    override fun close(params: IntentScreenParams<*>): Boolean {
        return false
    }

    override fun close(screenKey: ScreenKey): Boolean {
        return false
    }
}

@Serializable
private class SerializablePages<T : IntentScreenParams<*>>(
    val items: List<T>,
    val selectedIndex: Int,
)
