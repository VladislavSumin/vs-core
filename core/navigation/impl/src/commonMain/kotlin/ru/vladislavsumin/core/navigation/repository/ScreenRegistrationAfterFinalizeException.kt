package ru.vladislavsumin.core.navigation.repository

import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Выбрасывается при попытке зарегистрировать экран после завершения регистрации.
 */
public class ScreenRegistrationAfterFinalizeException internal constructor(screenKey: ScreenKey<*>) : Exception(
    "NavigationRegistry already finalized. Use NavigationRegistrar to navigation registration. ScreenKey = $screenKey",
)
