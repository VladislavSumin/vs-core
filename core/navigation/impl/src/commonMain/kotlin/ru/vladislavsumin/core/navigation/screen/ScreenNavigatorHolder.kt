package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.navigator.ScreenNavigatorImpl

/**
 * Грязный хак с передачей [ScreenNavigatorImpl] в [Screen], необходим для уменьшения аргументов которые нужно явно
 * передавать через фабрику.
 * При этом хак является безопасным, так как [Screen] может создаваться только на главном потоке, следовательно, мы
 * исключаем вероятность гонки потоков.
 */
internal var ScreenNavigatorHolder: ScreenNavigatorImpl<*>? = null
