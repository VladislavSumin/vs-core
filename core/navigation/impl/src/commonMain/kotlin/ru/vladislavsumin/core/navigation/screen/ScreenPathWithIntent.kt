package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.ScreenIntent

internal data class ScreenPathWithIntent(
    val screenPath: ScreenPath,
    val intent: ScreenIntent?,
)
