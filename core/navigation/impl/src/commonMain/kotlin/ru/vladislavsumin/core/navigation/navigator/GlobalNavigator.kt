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
    private val relay = UnsafeRelay()

    /**
     * Открывает экран соответствующий переданным [screenParams], при этом поиск пути производится относительно
     * переданного [screenPath]. (подробнее про поиск пути до экрана можно прочитать в документации).
     */
    fun open(screenPath: ScreenPath, screenParams: IntentScreenParams<*>, intent: ScreenIntent?) {
        NavigationLogger.i { "Open screen ${screenParams::class.simpleName}" }
        relay.accept {
            val path = createOpenPath(screenPath, screenParams)
            rootNavigator.openInsideThisScreen(path, intent)
        }
    }

    internal fun createOpenPath(screenPath: ScreenPath, screenParams: IntentScreenParams<*>): ScreenPath {
        val screenKey = screenParams.asKey()

        // Нода в графе навигации соответствующая переданному пути.
        val fromScreenNode: LinkedTreeNode<ScreenInfo> = navigation.navigationTree.findByPath(
            path = screenPath.map { it.asScreenKey() },
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
        relay.accept {
            // Находим ноду соответствующую экрану из которого пришел запрос.
            val currentNode = navigation.navigationTree.findByPath(
                path = screenPath.map { it.asScreenKey() },
                keySelector = { it.screenKey },
            )!!

            // Ищем экраны относительно текущего для определения порядка попыток закрытия.
            val finalPaths = currentNode.asSequenceUp()
                .filter { it.value.screenKey == screenParams.asKey() }
                .map { node ->
                    val path = node.path()
                        .dropLast(1) // убираем текущую ноду, так как ее параметры у нас в screenParams
                        .map { it.value.screenKey }
                        .map { ScreenPath.PathElement.Key(it) }
                    (ScreenPath(path) + screenParams).reachFrom(screenPath)
                }
            for (path in finalPaths) {
                if (rootNavigator.closeInsideThisScreen(ScreenPath(path.drop(1)))) {
                    return
                }
            }
        }
    }
}

/**
 * Упрощенный Relay из навигации Аркадия Иванова.
 * Служит для той же цели. Предотвратить навигацию внутри навигации.
 */
private class UnsafeRelay {
    private val queue = ArrayDeque<() -> Unit>()
    private var isDraining = false

    fun accept(value: () -> Unit) {
        queue.addLast(value)
        if (isDraining) {
            return
        }
        isDraining = true
        drainLoop()
    }

    private fun drainLoop() {
        while (true) {
            if (queue.isEmpty()) {
                isDraining = false
                return
            }
            queue.removeFirst().invoke()
        }
    }
}
