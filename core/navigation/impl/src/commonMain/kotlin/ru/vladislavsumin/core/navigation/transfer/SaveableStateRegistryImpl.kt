package ru.vladislavsumin.core.navigation.transfer

import androidx.compose.runtime.saveable.SaveableStateRegistry

/**
 * Реестр Compose-состояния ([SaveableStateRegistry]), работающий как прокси:
 *
 * 1. **In-memory transfer** — через [captureRaw] / restoredValues карта передаётся
 *    напрямую между holder'ами. Любые типы (Parcelable, объекты).
 * 2. **Process death (Android)** — после вызова [attachPlatform] делегирует
 *    [performSave] / [consumeRestored] платформенному registry (Bundle/Parcelable).
 * 3. **Process death (Desktop)** — без платформы [performSave] возвращает пустую карту,
 *    Compose-состояние при смерти процесса не сохраняется.
 */
internal class SaveableStateRegistryImpl(
    restoredValues: Map<String, List<Any?>>,
    private var platform: SaveableStateRegistry? = null,
) : SaveableStateRegistry {

    private val restored = restoredValues.toMutableMap()
    private val entries = linkedMapOf<String, MutableList<EntryImpl>>()

    /**
     * Прикрепляет платформенный registry (Android: из [LocalSaveableStateRegistry.current]).
     * После вызова [performSave] / [consumeRestored] начинают делегировать платформе.
     */
    fun attachPlatform(newPlatform: SaveableStateRegistry?) {
        if (platform === newPlatform) return
        platform = newPlatform
        entries.values.flatten().forEach { entry ->
            entry.platformEntry?.unregister()
            entry.platformEntry = platform?.registerProvider(entry.key, entry.valueProvider)
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

    override fun canBeSaved(value: Any): Boolean = platform?.canBeSaved(value) ?: true

    override fun registerProvider(key: String, valueProvider: () -> Any?): SaveableStateRegistry.Entry {
        val entry = EntryImpl(key, valueProvider)
        entry.platformEntry = platform?.registerProvider(key, valueProvider)
        entries.getOrPut(key) { mutableListOf() }.add(entry)
        return entry
    }

    override fun performSave(): Map<String, List<Any?>> = platform?.performSave() ?: emptyMap()

    /**
     * Захватывает «сырые» in-memory значения для передачи при transfer.
     * Формат хранения соответствует стандартному Compose [SaveableStateRegistry.performSave]:
     * каждое значение провайдера оборачивается в [listOf] (без разворачивания).
     */
    fun captureRaw(): Map<String, List<Any?>> = entries.mapValues { (_, list) -> list.map { it.valueProvider() } }

    private inner class EntryImpl(val key: String, val valueProvider: () -> Any?,) : SaveableStateRegistry.Entry {
        var platformEntry: SaveableStateRegistry.Entry? = null

        override fun unregister() {
            platformEntry?.unregister()
            val list = entries[key]
            list?.remove(this)
            if (list.isNullOrEmpty()) {
                entries.remove(key)
            }
        }
    }
}
