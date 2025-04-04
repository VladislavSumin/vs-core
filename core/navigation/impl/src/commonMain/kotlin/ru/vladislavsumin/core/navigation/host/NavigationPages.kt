package ru.vladislavsumin.core.navigation.host

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.ScreenParams
import ru.vladislavsumin.core.navigation.navigator.HostNavigator
import ru.vladislavsumin.core.navigation.screen.Screen
import ru.vladislavsumin.core.navigation.screen.ScreenContext
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Навигация типа "страницы", означает что в ней одновременно может быть несколько экранов, но только один из них
 * активен, при этом в отличие от стека активен может быть любой экран, а не только последний
 *
 * @param navigationHost навигационный хост для возможности понять какие экраны будут открываться в этой навигации.
 * @param initialPages начальный набор страниц.
 * @param key уникальный в пределах экрана ключ для навигации.
 * @param handleBackButton будет ли эта навигация перехватывать нажатия назад.
 */
public fun ScreenContext.childNavigationPages(
    navigationHost: NavigationHost,
    initialPages: () -> Pages<ScreenParams>,
    key: String = "pages_navigation",
    handleBackButton: Boolean = false,
): Value<ChildPages<ScreenParams, Screen>> {
    val source = PagesNavigation<ScreenParams>()

    val hostNavigator = PagesHostNavigator(source)
    navigator.registerHostNavigator(navigationHost, hostNavigator)

    val stack = childPages(
        source = source,
        serializer = navigator.serializer,
        key = key,
        initialPages = {
            navigator.getInitialParamsFor(navigationHost)?.let { Pages(listOf(it), 0) } ?: initialPages()
        },
        handleBackButton = handleBackButton,
        childFactory = ::childScreenFactory,
    )
    return stack
}

private class PagesHostNavigator(
    @Suppress("UnusedPrivateProperty")
    private val pagesNavigation: PagesNavigation<ScreenParams>,
) : HostNavigator {
    override fun open(params: ScreenParams) {
        // Если такого экрана еще нет в стеке, то открываем его.
        // Если же экран уже есть в стеке, то закрываем все экраны после него.
//        pagesNavigation.navigate(
//            transformer = { stack ->
//                val indexOfScreen = stack.indexOf(params)
//                if (indexOfScreen >= 0) {
//                    stack.subList(0, indexOfScreen + 1)
//                } else {
//                    stack + params
//                }
//            },
//            onComplete = { _, _ -> },
//        )
        TODO()
    }

    override fun open(
        screenKey: ScreenKey<*>,
        defaultParams: () -> ScreenParams,
    ) {
        // Если экрана с таким ключом еще нет в стеке, то открываем его используя defaultParams.
        // Если же экран с таким ключом уже есть в стеке, то закрываем все экраны после него.
//        pagesNavigation.navigate(
//            transformer = { stack ->
//                val indexOfScreen = stack.map { it.asErasedKey() }.indexOf(screenKey)
//                if (indexOfScreen >= 0) {
//                    stack.subList(0, indexOfScreen + 1)
//                } else {
//                    stack + defaultParams()
//                }
//            },
//            onComplete = { _, _ -> },
//        )
        TODO()
    }

    override fun close(params: ScreenParams): Boolean {
        // Если закрываемый экран расположен первым, то закрываем все экраны КРОМЕ этого, так как в стеке должен быть
        // хотя бы один экран.
        // Если закрываемый экран расположен вторым или далее, то закрываем этот экран и все после него.
        // Если закрываемого экрана нет в стеке, то ничего не делаем.
//        var isSuccess: Boolean? = null
//        pagesNavigation.navigate(
//            transformer = { stack ->
//                val indexOfScreen = stack.indexOf(params)
//                if (indexOfScreen >= 0) {
//                    isSuccess = false
//                    stack.subList(0, indexOfScreen)
//                } else {
//                    if (indexOfScreen == 0) {
//                        isSuccess = false
//                        listOf(params)
//                    } else {
//                        isSuccess = true
//                        stack.subList(0, indexOfScreen)
//                    }
//                }
//            },
//            onComplete = { _, _ -> },
//        )
//        return isSuccess ?: error("unreachable")
        TODO()
    }

    override fun close(screenKey: ScreenKey<ScreenParams>): Boolean {
        // То же самое как при закрытии по инстансу экрана, но ищем по ключу с конца стека до первого найденного экрана.
//        var isSuccess: Boolean? = null
//        pagesNavigation.navigate(
//            transformer = { stack ->
//                val keysStack = stack.map { it.asErasedKey() }
//                val indexOfScreen = keysStack.indexOfLast { it == screenKey }
//                if (indexOfScreen >= 0) {
//                    isSuccess = false
//                    stack.subList(0, indexOfScreen)
//                } else {
//                    if (indexOfScreen == 0) {
//                        isSuccess = false
//                        stack.subList(0, 1)
//                    } else {
//                        isSuccess = true
//                        stack.subList(0, indexOfScreen)
//                    }
//                }
//            },
//            onComplete = { _, _ -> },
//        )
//        return isSuccess ?: error("unreachable")
        TODO()
    }
}
