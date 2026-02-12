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
import ru.vladislavsumin.core.navigation.screen.ScreenPath
import ru.vladislavsumin.core.navigation.screen.asKey
import ru.vladislavsumin.core.navigation.tree.ScreenInfo

/**
 * Глобальный навигатор.
 */
internal class GlobalNavigator<Ctx : GenericComponentContext<Ctx>>(
    private val navigation: GenericNavigation<Ctx>,
) {

    internal lateinit var rootNavigator: ScreenNavigatorImpl<Ctx>
    private val relay = UnsafeRelay()

    /**
     * Открывает экран соответствующий переданным [targetScreenParams], при этом поиск пути производится относительно
     * переданного [startScreenPath]. (подробнее про поиск пути до экрана можно прочитать в документации).
     */
    fun open(startScreenPath: ScreenPath, targetScreenParams: IntentScreenParams<*>, intent: ScreenIntent?) {
        NavigationLogger.i { "Open screen ${targetScreenParams::class.simpleName}" }
        relay.accept {
            val screenPath = createOpenPath(startScreenPath, targetScreenParams)
            rootNavigator.openChain(screenPath, intent)
        }
    }

    /**
     * Ищет путь к экрану [targetScreenParams] начиная поиск от [startScreenPath].
     */
    fun createOpenPath(startScreenPath: ScreenPath, targetScreenParams: IntentScreenParams<*>): ScreenPath {
        val targetScreenKey = targetScreenParams.asKey()

        // Нода в графе навигации соответствующая переданному пути.
        val startSearchScreenNode: LinkedTreeNode<ScreenInfo<Ctx>> = navigation.navigationTree.findByPath(
            path = startScreenPath.map { it.asScreenKey() },
            keySelector = { it.screenKey },
        )!!

        // Нода в графе навигации куда мы хотим перейти.
        val destinationNode: LinkedTreeNode<ScreenInfo<Ctx>> = startSearchScreenNode
            .asSequenceUp()
            .first { node -> node.value.screenKey == targetScreenKey }

        // Путь до искомой ноды.
        val destinationKeysPath: List<ScreenPath.PathElement> = destinationNode.path()
            .map { node -> ScreenPath.PathElement.Key(node.value.screenKey) }
        return ScreenPath(
            destinationKeysPath
                .drop(1) // Исключаем первый (рутовый) экран.
                .dropLast(1) // Исключаем последний (целевой) экран.
                // Добавляем целевой экран уже как параметр.
                .plus(ScreenPath.PathElement.Params(targetScreenParams)),
        )
    }

    fun close(startScreenPath: ScreenPath, targetScreenParams: IntentScreenParams<*>) {
        NavigationLogger.i { "Close screen ${targetScreenParams::class.simpleName}" }
        relay.accept {
            // Находим ноду соответствующую экрану из которого пришел запрос.
            val currentNode = navigation.navigationTree.findByPath(
                path = startScreenPath.map { it.asScreenKey() },
                keySelector = { it.screenKey },
            )!!

            // Ищем экраны относительно текущего для определения порядка попыток закрытия.
            val finalPaths = currentNode.asSequenceUp()
                .filter { it.value.screenKey == targetScreenParams.asKey() }
                .map { node ->
                    val path = node.path()
                        .dropLast(1) // убираем текущую ноду, так как ее параметры у нас в screenParams
                        .map { it.value.screenKey }
                        .map { ScreenPath.PathElement.Key(it) }
                    (ScreenPath(path) + targetScreenParams).reachFrom(startScreenPath)
                }
            for (path in finalPaths) {
                if (rootNavigator.closeChain(ScreenPath(path.drop(1)))) {
                    return@accept
                }
            }
        }
    }

    /**
     * При восстановлении состояния экранов тоже может произойти навигационное событие внутри создания состояний
     * decompose. Такое поведение недопустимо в нашем случае, поэтому запускаем восстановление через [relay], тогда
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
