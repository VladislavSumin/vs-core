package ru.vladislavsumin.core.navigation.repository

import ru.vladislavsumin.core.navigation.screen.ScreenKey

/**
 * Выбрасывается при попытке зарегистрировать один экран несколько раз в разных навигационных хостах одного родителя.
 */
public class MultipleScreenRegistrationInSameParentException internal constructor(
    parentScreen: ScreenKey<*>,
    childScreen: ScreenKey<*>,
) : Exception("Child screen $childScreen, registered in parent screen $parentScreen more than once")
