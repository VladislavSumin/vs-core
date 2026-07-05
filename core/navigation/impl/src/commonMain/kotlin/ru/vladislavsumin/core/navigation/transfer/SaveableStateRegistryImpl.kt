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

    override fun consumeRestored(key: String): Any? {
        val values = restored[key] ?: return platform?.consumeRestored(key)
        if (values.isEmpty()) return null
        val result = values[0]
        if (values.size > 1) {
            restored[key] = values.subList(1, values.size)
        } else {
            restored.remove(key)
        }
        return result
    }

    override fun canBeSaved(value: Any): Boolean =
        platform?.canBeSaved(value) ?: true

    override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry {
        providers[key] = valueProvider
        platformEntries[key] = platform?.registerProvider(key, valueProvider)
        return Entry(key)
    }

    override fun performSave(): Map<String, List<Any?>> =
        platform?.performSave() ?: emptyMap()

    /**
     * Захватывает «сырые» in-memory значения для передачи при transfer.
     * Формат хранения соответствует стандартному Compose [SaveableStateRegistry.performSave]:
     * каждое значение провайдера оборачивается в [listOf] (без разворачивания).
     */
    fun captureRaw(): Map<String, List<Any?>> =
        providers.mapValues { (_, provider) -> listOf(provider()) }

    private inner class Entry(private val key: String) : SaveableStateRegistry.Entry {
        override fun unregister() {
            providers.remove(key)
            platformEntries.remove(key)?.unregister()
        }
    }
}
