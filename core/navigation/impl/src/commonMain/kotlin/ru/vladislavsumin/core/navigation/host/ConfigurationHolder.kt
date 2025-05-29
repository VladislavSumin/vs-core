package ru.vladislavsumin.core.navigation.host

import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

/**
 * Содержит конфигурацию экрана и дополнительную метаинформацию для организации взаимодействия.
 */
internal data class ConfigurationHolder(
    val screenParams: IntentScreenParams<*>,
    val intents: Channel<ScreenIntent> = Channel(Channel.BUFFERED),
)
