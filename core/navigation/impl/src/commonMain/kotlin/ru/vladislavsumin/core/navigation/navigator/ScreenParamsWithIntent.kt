package ru.vladislavsumin.core.navigation.navigator

import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

internal data class ScreenParamsWithIntent(
    val screenParams: IntentScreenParams<*>,
    val intent: ScreenIntent?,
    val savedInstance: TransferableScreenHolder<*>? = null,
)
