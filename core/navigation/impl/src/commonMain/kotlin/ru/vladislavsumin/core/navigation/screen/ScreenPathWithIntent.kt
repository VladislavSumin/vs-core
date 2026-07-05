package ru.vladislavsumin.core.navigation.screen

import ru.vladislavsumin.core.navigation.ScreenIntent
import ru.vladislavsumin.core.navigation.transfer.TransferableScreenHolder

internal data class ScreenPathWithIntent(
    val screenPath: ScreenPath,
    val intent: ScreenIntent?,
    val savedInstance: TransferableScreenHolder<*>? = null,
)
