package ru.vladislavsumin.core.navigation.repository

import ru.vladislavsumin.core.navigation.NavigationHost
import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Выбрасывается при попытке повторной регистрации хоста.
 */
public class DoubleHostRegistrationException internal constructor(
    screenKey: ScreenKey<*>,
    navigationHost: NavigationHost,
) : Exception("Double registration for ScreenKey=$screenKey, host=$navigationHost")
