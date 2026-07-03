package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Бросается когда при открытии экрана [targetScreenKey] в графе навигации не удалось найти путь, в котором подсказки
 * [hints] встречаются среди предков открываемого экрана в виде упорядоченной подпоследовательности.
 */
public class NoScreenMatchingHintsException internal constructor(
    targetScreenKey: ScreenKey,
    hints: List<IntentScreenParams<*>>,
) : Exception(
    "No path to screen ${targetScreenKey.key.simpleName} matching hints " +
        hints.joinToString(prefix = "[", postfix = "]") { it::class.simpleName.toString() },
)
