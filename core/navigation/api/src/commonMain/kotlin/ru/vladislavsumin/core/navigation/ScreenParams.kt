package ru.vladislavsumin.core.navigation

/**
 * Параметры для запуска экрана.
 *
 * При этом [IntentScreenParams::class] является ключом экрана, с его помощью можно регистрировать хосты навигации,
 * а также регистрировать экран в хостах навигации.
 *
 * Инстанс объекта [IntentScreenParams] напротив является ключом конкретного инстанса экрана и может использоваться для
 * передачи конфигурации. Если экран не требует параметров конфигурации, то его [IntentScreenParams] могут быть
 * `data object`. Если два инстанса экрана равны по equals, то они будут соответствовать одному и тому же экрану.
 */
public interface IntentScreenParams<I : ScreenIntent>

/**
 * Реализация [IntentScreenParams] без поддержки интентов. Нужна для упрощенного создания параметров экрана, если
 * события не нужны.
 */
public interface ScreenParams : IntentScreenParams<NoIntent>
