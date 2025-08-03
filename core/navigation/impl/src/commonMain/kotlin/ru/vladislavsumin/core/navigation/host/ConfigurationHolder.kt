package ru.vladislavsumin.core.navigation.host

import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.ScreenIntent

/**
 * Содержит конфигурацию экрана и дополнительную метаинформацию для организации взаимодействия.
 */
public class ConfigurationHolder internal constructor(
    public val screenParams: IntentScreenParams<out ScreenIntent>,
    internal val intents: Channel<ScreenIntent> = Channel(Channel.BUFFERED),
) {

    // Используем обычный класс вместо data class что бы не выставлять наружу copy метод.
    // При ручной генерации equals && hashCode учитываем только screenParams так как они по сути
    // являются ключом экрана, в то время как другие параметры просто мета данные.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ConfigurationHolder

        return screenParams == other.screenParams
    }

    override fun hashCode(): Int {
        return screenParams.hashCode()
    }
}
