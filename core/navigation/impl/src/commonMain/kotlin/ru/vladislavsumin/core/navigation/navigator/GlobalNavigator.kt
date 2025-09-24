package ru.vladislavsumin.core.navigation.navigator

import com.arkivanov.decompose.GenericComponentContext
import ru.vladislavsumin.core.collections.tree.LinkedTreeNode
import ru.vladislavsumin.core.collections.tree.asSequenceUp
import ru.vladislavsumin.core.collections.tree.findByPath
import ru.vladislavsumin.core.collections.tree.path
import ru.vladislavsumin.core.navigation.GenericNavigation
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.NavigationLogger
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.screen.Render
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

/**
 * Глобальный навигатор.
 */
internal class GlobalNavigator<Ctx : GenericComponentContext<Ctx>, R : Render>(
    private val navigation: GenericNavigation<Ctx, R>,
) {

    internal lateinit var rootNavigator: ScreenNavigator<Ctx, R>
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

    fun createOpenPath(screenPath: ScreenPath, screenParams: IntentScreenParams<*>): ScreenPath {
        val screenKey = screenParams.asKey()

        // Нода в графе навигации соответствующая переданному пути.
        val fromScreenNode: LinkedTreeNode<ScreenInfo<Ctx, R>> = navigation.navigationTree.findByPath(
            path = screenPath.map { it.asScreenKey() },
            keySelector = { it.screenKey },
        )!!

        // Нода в графе навигации куда мы хотим перейти.
        val destinationNode: LinkedTreeNode<ScreenInfo<Ctx, R>> = fromScreenNode
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
                    return@accept
                }
            }
        }
    }

    /**
     * При восстановлении состояния экранов тоже может произойти навигационное событие внутри создания состояний
     * decompose. Такое поведение недопустимо в нашем случае поэтому запускаем восстановление через [relay], тогда
     * при возникновении навигационных событий они будут выполнены только после завершения восстановления.
     */
    fun protectRestoreState(action: () -> Unit) {
        relay.accept(action)
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
