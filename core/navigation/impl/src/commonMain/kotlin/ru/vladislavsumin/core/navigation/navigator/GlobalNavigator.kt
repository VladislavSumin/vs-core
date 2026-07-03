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
import kotlin.time.measureTimedValue

/**
 * Глобальный навигатор.
 */
internal class GlobalNavigator<Ctx : GenericComponentContext<Ctx>>(private val navigation: GenericNavigation<Ctx>) {

    internal lateinit var rootNavigator: ScreenNavigatorImpl<Ctx>
    private val relay = UnsafeRelay()

    /**
     * Открывает экран соответствующий переданным [targetScreenParams], при этом поиск пути производится относительно
     * переданного [startScreenPath]. (подробнее про поиск пути до экрана можно прочитать в документации).
     *
     * @param hints необязательный список подсказок — параметров экранов, которые должны встретиться среди предков
     * открываемого экрана в виде упорядоченной подпоследовательности (в том же порядке, но не обязательно все и с
     * возможными разрывами). Позволяет снять неоднозначность когда целевой экран зарегистрирован в нескольких местах
     * графа, а так же закрепить конкретные инстансы родительских экранов.
     */
    fun open(
        startScreenPath: ScreenPath,
        targetScreenParams: IntentScreenParams<*>,
        intent: ScreenIntent?,
        hints: List<IntentScreenParams<*>>,
    ) {
        NavigationLogger.i { "Open screen ${targetScreenParams::class.simpleName}" }
        relay.accept {
            val screenPath = measureTimedValue {
                createOpenPath(startScreenPath, targetScreenParams, hints)
            }
            NavigationLogger.d { "Screen path calculated at ${screenPath.duration}" }
            rootNavigator.openChain(screenPath.value, intent)
        }
    }

    /**
     * Ищет путь к экрану [targetScreenParams] начиная поиск от [startScreenPath].
     *
     * @param startScreenPath путь от корня навигации до экрана с которого было соверщено навигационное действие.
     * @param hints необязательный список подсказок, ограничивающих выбор пути (см. [open]).
     */
    fun createOpenPath(
        startScreenPath: ScreenPath,
        targetScreenParams: IntentScreenParams<*>,
        hints: List<IntentScreenParams<*>>,
    ): ScreenPath {
        val targetScreenKey = targetScreenParams.asKey()

        // Нода в графе навигации соответствующая переданному пути.
        val startSearchScreenNode: LinkedTreeNode<ScreenInfo<Ctx>> = navigation.navigationTree.findByPath(
            path = startScreenPath.map { it.asScreenKey() },
            keySelector = { it.screenKey },
        )!!

        // Среди всех кандидатов (в порядке "вниз, потом вверх") выбираем первого, чей путь предков содержит подсказки
        // в виде упорядоченной подпоследовательности.
        var hintAlignment: Map<Int, IntentScreenParams<*>>? = null
        val destinationNode: LinkedTreeNode<ScreenInfo<Ctx>> = startSearchScreenNode
            .asSequenceUp()
            .filter { node -> node.value.screenKey == targetScreenKey }
            .firstOrNull { node ->
                val alignment = matchHints(node.path().dropLast(1), hints)
                if (alignment != null) hintAlignment = alignment
                alignment != null
            }
            ?: throw NoScreenMatchingHintsException(targetScreenKey, hints)

        val alignment = hintAlignment!!

        val destinationKeysPath: List<ScreenPath.PathElement> = destinationNode.path()
            .dropLast(1) // Исключаем последний (целевой) экран.
            .mapIndexed { index, node ->
                // Приоритет: подсказка > оригинальный инстанс из текущего пути > ключ.
                // Почему нужно по возможности заменять ноды в пути оригинальными?
                // Потому что у нас может быть цепочка включающая экраны с параметрами для которых открыто более
                // одного инстанса. Тогда потеряв информацию о инстансах экранов мы можем открыть искомый экран не
                // в том экране в котором хотим.
                alignment[index]?.let { ScreenPath.PathElement.Params(it) }
                    ?: startScreenPath.getOrNull(index)
                    ?: ScreenPath.PathElement.Key(node.value.screenKey)
            }
            .drop(1) // Исключаем первый (рутовый) экран.
            // Добавляем целевой экран уже как параметр.
            .plus(ScreenPath.PathElement.Params(targetScreenParams))
        return ScreenPath(destinationKeysPath)
    }

    /**
     * Пытается сопоставить [hints] с [ancestors] (предками целевого экрана) как упорядоченную подпоследовательность
     * (жадно, по первому совпадению).
     *
     * @return отображение "индекс предка -> параметры подсказки" если все подсказки найдены, иначе null.
     */
    private fun matchHints(
        ancestors: List<LinkedTreeNode<ScreenInfo<Ctx>>>,
        hints: List<IntentScreenParams<*>>,
    ): Map<Int, IntentScreenParams<*>>? {
        if (hints.isEmpty()) return emptyMap()
        val alignment = mutableMapOf<Int, IntentScreenParams<*>>()
        var hintIndex = 0
        for ((ancestorIndex, node) in ancestors.withIndex()) {
            if (hintIndex >= hints.size) break
            if (node.value.screenKey == hints[hintIndex].asKey()) {
                alignment[ancestorIndex] = hints[hintIndex]
                hintIndex++
            }
        }
        return if (hintIndex == hints.size) alignment else null
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
