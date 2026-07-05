package ru.vladislavsumin.core.navigation.transfer

import androidx.compose.runtime.saveable.SaveableStateRegistry

/**
 * Реестр Compose-состояния ([SaveableStateRegistry]), работающий как прокси:
 *
 * 1. **In-memory transfer** — через [captureRaw] / restoredValues карта передаётся
 *    напрямую между holder'ами. Любые типы (Parcelable, объекты).
 * 2. **Process death (Android)** — после вызова [attachPlatform] делегирует
 *    [performSave] / [consumeRestored] платформенному registry (Bundle/Parcelable).
 * 3. **Process death (Desktop)** — без платформы сохраняет только сериализуемые типы
 *    через [stateKeeper] (Json).
 */
internal class SaveableStateRegistryImpl(
    restoredValues: Map<String, List<Any?>>,
    private var platform: SaveableStateRegistry? = null,
) : SaveableStateRegistry {

    private val restored = restoredValues.toMutableMap()
    private val providers = linkedMapOf<String, () -> Any?>()
    private val platformEntries = mutableMapOf<String, SaveableStateRegistry.Entry?>()

    /**
     * Прикрепляет платформенный registry (Android: из [LocalSaveableStateRegistry.current]).
     * После вызова [performSave] / [consumeRestored] начинают делегировать платформе.
     */
    fun attachPlatform(newPlatform: SaveableStateRegistry?) {
        if (platform === newPlatform) return
        platform = newPlatform
        // Перерегистрируем провайдеры в новой платформе
        providers.forEach { (key, provider) ->
            platformEntries[key]?.unregister()
            platformEntries[key] = platform?.registerProvider(key, provider)
        }
    }

    override fun consumeRestored(key: String): Any? =
        restored.remove(key)?.let { values ->
            if (values.size == 1) values.single() else values
        } ?: platform?.consumeRestored(key)

    override fun canBeSaved(value: Any): Boolean =
        platform?.canBeSaved(value) ?: true

    override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry {
        providers[key] = valueProvider
        platformEntries[key] = platform?.registerProvider(key, valueProvider)
        return Entry(key)
    }

    override fun performSave(): Map<String, List<Any?>> {
        val platformMap = platform?.performSave() ?: emptyMap()
        val rawMap = rawMap()
        return platformMap + rawMap
    }

    /**
     * Захватывает «сырые» in-memory значения для передачи при transfer.
     * Не требует сериализации.
     */
    fun captureRaw(): Map<String, List<Any?>> = rawMap()

    private fun rawMap(): Map<String, List<Any?>> =
        providers.mapValues { (_, provider) ->
            val value = provider()
            if (value is List<*>) value as List<Any?> else listOf(value)
        }

    private inner class Entry(private val key: String) : SaveableStateRegistry.Entry {
        override fun unregister() {
            providers.remove(key)
            platformEntries.remove(key)?.unregister()
        }
    }
}
