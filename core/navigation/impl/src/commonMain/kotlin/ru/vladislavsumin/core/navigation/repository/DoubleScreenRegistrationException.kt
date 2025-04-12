package ru.vladislavsumin.core.navigation.repository

import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Выбрасывается при попытке повторной регистрации экрана.
 */
public class DoubleScreenRegistrationException internal constructor(screenKey: ScreenKey<*>) :
    Exception("Double registration for key=$screenKey")
