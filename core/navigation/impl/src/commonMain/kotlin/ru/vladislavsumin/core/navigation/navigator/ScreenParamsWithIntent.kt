package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

internal data class ScreenParamsWithIntent(
    val screenParams: IntentScreenParams<*>,
    val intent: ScreenIntent?,
)
