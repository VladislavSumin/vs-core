package ru.vladislavsumin.core.navigation.tree

import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.collections.tree.LinkedTreeNodeImpl
import ru.vladislavsumin.core.collections.tree.linkedNodeOf
import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.repository.NavigationRepository
import ru.vladislavsumin.core.navigation.screen.ScreenKey

internal class NavigationTreeBuilder(
    private val repository: NavigationRepository,
) {
    fun build() = NavigationTree(buildNavGraph())

    /**
     * Строит навигационное дерево.
     *
     * @return возвращает корень полученного дерева.
     */
    private fun buildNavGraph(): LinkedTreeNode<ScreenInfo> {
        val rootScreen = findRootScreen()
        return buildNode(
            parent = null,
            hostInParent = null,
            screenKey = rootScreen,
        )
    }

    /**
     * Рекурсивная функция которая строит дерево.
     *
     * @param parent родительская нода, нужна для создания дерева с возможностью перемещаться вверх, null для головной
     * ноды дерева.
     * @param hostInParent родительский хост внутри которого находится данная нода.
     * @param screenKey ключ соответствующий [Node] которую нужно создать.
     */
    private fun buildNode(
        parent: ScreenKey<*>?,
        hostInParent: NavigationHost?,
        screenKey: ScreenKey<*>,
    ): LinkedTreeNodeImpl<ScreenInfo> {
        val screenRegistration = repository.screens[screenKey]
            ?: throw ScreenNotRegisteredException(parent, hostInParent, screenKey)

        val screenInfo = ScreenInfo(
            screenKey = screenKey,
            hostInParent = hostInParent,
            factory = screenRegistration.factory,
            defaultParams = screenRegistration.defaultParams,
            description = screenRegistration.description,
            navigationHosts = screenRegistration.navigationHosts.keys,
        )

        // Пробегаемся по всем навигационным хостам, объявленным для данной ноды.
        val child: List<LinkedTreeNodeImpl<ScreenInfo>> = screenRegistration.navigationHosts
            .flatMap { (host, screens) ->
                screens.map { screen -> buildNode(screenKey, host, screen) }
            }

        return linkedNodeOf(screenInfo, children = child)
    }

    /**
     * Ищет root screen, этим экраном является такой экран который невозможно открыть из другой точки графа.
     */
    private fun findRootScreen(): ScreenKey<*> {
        val nonRootScreens = repository.screens.values.map { registration ->
            registration.navigationHosts.values.flatten()
        }.flatten()

        // Множество экранов, у которых нет точек входа (множество рутовых экранов)
        val roots = repository.screens.keys - nonRootScreens

        check(roots.size == 1) {
            val formattedRoots = roots.joinToStingFormatted { it.key.simpleName!! }
            "Found more than one root or no root found, roots:\n$formattedRoots"
        }
        return roots.first()
    }
}

// TODO вынести в core модуль
/**
 * Форматирует последовательность в строку добавляя форматирование по умолчанию.
 */
private fun <T> Iterable<T>.joinToStingFormatted(transform: ((T) -> CharSequence)? = null): String {
    return joinToString(
        prefix = "[\n\t",
        postfix = "\n]",
        separator = ",\n\t",
        transform = transform,
    )
}
