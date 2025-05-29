package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.collections.tree.asSequenceUp
import ru.vladislavsumin.core.collections.tree.findByPath
import ru.vladislavsumin.core.collections.tree.path
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.NavigationLogger
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

/**
 * Глобальный навигатор.
 */
internal class GlobalNavigator(
    private val navigation: Navigation,
) {

    internal lateinit var rootNavigator: ScreenNavigator

    /**
     * Открывает экран соответствующий переданным [screenParams], при этом поиск пути производится относительно
     * переданного [screenPath]. (подробнее про поиск пути до экрана можно прочитать в документации).
     */
    fun open(screenPath: ScreenPath, screenParams: IntentScreenParams<*>, intent: ScreenIntent?) {
        NavigationLogger.i { "Open screen ${screenParams::class.simpleName}" }
        val path = createOpenPath(screenPath, screenParams)
        rootNavigator.openInsideThisScreen(path)
    }

    internal fun createOpenPath(screenPath: ScreenPath, screenParams: IntentScreenParams<*>): ScreenPath {
        val screenKey = screenParams.asKey()

        // Нода в графе навигации соответствующая переданному пути.
        val fromScreenNode: LinkedTreeNode<ScreenInfo> = navigation.navigationTree.findByPath(
            path = screenPath.map { it.asErasedKey() },
            keySelector = { it.screenKey },
        )!!

        // Нода в графе навигации куда мы хотим перейти.
        val destinationNode: LinkedTreeNode<ScreenInfo> = fromScreenNode
            .asSequenceUp()
            .first { node -> node.value.screenKey == screenKey }

        // Путь до искомой ноды.
        val destinationKeysPath: List<ScreenPath.PathElement.Key> = destinationNode.path()
            .map { node -> node.value.screenKey }
            .map { ScreenPath.PathElement.Key(it) }
        return ScreenPath(destinationKeysPath.drop(1).dropLast(1) + ScreenPath.PathElement.Params(screenParams))
    }

    fun close(screenPath: ScreenPath, screenParams: IntentScreenParams<*>) {
        NavigationLogger.i { "Close screen ${screenParams::class.simpleName}" }
        val index = screenPath.indexOfLast { it == ScreenPath.PathElement.Params(screenParams) }
        if (index == -1) return
        val path = screenPath.subList(0, index)
        rootNavigator.closeInsideThisScreen(ScreenPath(path.drop(1) + ScreenPath.PathElement.Params(screenParams)))
    }
}
