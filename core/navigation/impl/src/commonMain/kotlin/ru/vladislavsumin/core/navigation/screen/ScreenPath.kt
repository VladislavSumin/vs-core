package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.screen.ScreenPath.PathElement
import ru.vladislavsumin.core.navigation.screen.ScreenPath.PathElement.Params

/**
 * Цепочка пути до экрана.
 */
internal data class ScreenPath(val path: List<PathElement>) : List<PathElement> by path {

    constructor(screenParams: Iterable<IntentScreenParams<*>>) : this(screenParams.map { Params(it) })

    constructor(screenParams: IntentScreenParams<*>) : this(listOf(screenParams))

    operator fun plus(screenParams: IntentScreenParams<*>): ScreenPath {
        return ScreenPath(path + Params(screenParams))
    }

    fun parent() = ScreenPath(dropLast(1))

    /**
     * Обогащает путь к экрану известными параметрами из [otherPath]
     */
    fun reachFrom(otherPath: ScreenPath): ScreenPath {
        // Идентификатор "расхождения путей"
        var isPathConsistent = true

        val path = mapIndexed { index, element ->
            when (element) {
                is PathElement.Key -> {
                    if (isPathConsistent) {
                        val otherPathElement = otherPath.getOrNull(index)
                        if (otherPathElement?.asScreenKey() == element.screenKey) {
                            otherPathElement
                        } else {
                            isPathConsistent = false
                            element
                        }
                    } else {
                        element
                    }
                }

                is Params -> element
            }
        }
        return ScreenPath(path)
    }

    /**
     * Элемент пути, может быть ключом экрана, а может быть параметрами конкретного инстанса экрана.
     */
    sealed interface PathElement {
        fun asScreenKey(): ScreenKey

        data class Key(val screenKey: ScreenKey) : PathElement {
            override fun asScreenKey(): ScreenKey = screenKey
        }

        data class Params(val screenParams: IntentScreenParams<*>) : PathElement {
            override fun asScreenKey(): ScreenKey = screenParams.asKey()
        }
    }
}
